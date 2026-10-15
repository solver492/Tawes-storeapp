# Guide — Activer le bouton ✦ Personnaliser (génération d'images FLUX)

> Ce guide t'amène de zéro à un bouton **✦ Personnaliser** fonctionnel dans le back office.  
> Temps estimé : **15 à 20 minutes** (dont ~5 min d'attente de démarrage GPU).

---

## Comment ça marche en résumé

```
Back office (port 4000)
       │
       │  POST /generate  { prompt, source_image_url, n }
       ▼
  Notebook Google Colab  (GPU gratuit T4)
  ← expose une API via ngrok (URL publique temporaire)
       │
       │  retourne { images: [{ b64, width, height, seed }] }
       ▼
Back office → affiche les variantes → tu valides → ajouté aux médias du produit
```

Le notebook Colab tourne **FLUX.1-schnell** (4 steps, ~20 s/image sur T4).  
Le back office contacte Colab via l'URL **ngrok** que le notebook génère à chaque démarrage.

---

## Étape 1 — Ouvrir le notebook Google Colab

1. Va sur [https://colab.research.google.com](https://colab.research.google.com)
2. Clique **Fichier → Nouveau notebook**
3. En haut à droite, clique **Modifier le type d'exécution** → choisis **T4 GPU** → Enregistrer

---

## Étape 2 — Coller le code du notebook

Crée une seule cellule de code et colle exactement ce contenu :

```python
# ============================================================
# Tawes — Serveur FLUX.1-schnell pour le back office
# Génération d'images produit via prompt texte + image source
# ============================================================

# --- Installation des dépendances ---
!pip install -q diffusers transformers accelerate pyngrok flask pillow requests

import os, io, base64, threading, time
from flask import Flask, request, jsonify
from PIL import Image
import requests as req_lib
import torch
from diffusers import FluxImg2ImgPipeline
from pyngrok import ngrok, conf

# --- Paramètres ---
NGROK_TOKEN = ""          # ← colle ton token ngrok ici (voir étape 3)
PORT        = 7860

# --- Authentification ngrok ---
if not NGROK_TOKEN:
    raise ValueError("⛔  Colle ton token ngrok dans NGROK_TOKEN avant de lancer !")
conf.get_default().auth_token = NGROK_TOKEN
ngrok.kill()

# --- Chargement du modèle FLUX schnell ---
print("⏳ Chargement FLUX.1-schnell (première fois ~3 min, ensuite depuis le cache)...")
pipe = FluxImg2ImgPipeline.from_pretrained(
    "black-forest-labs/FLUX.1-schnell",
    torch_dtype=torch.bfloat16,
).to("cuda")
pipe.enable_model_cpu_offload()
print("✅ Modèle chargé !")

# --- Serveur Flask ---
app = Flask(__name__)

@app.route("/ping")
def ping():
    return jsonify({"ok": True})

@app.route("/generate", methods=["POST"])
def generate():
    data      = request.get_json(force=True)
    prompt    = data.get("prompt", "").strip()
    src_url   = data.get("source_image_url", "")
    width     = int(data.get("width", 768))
    height    = int(data.get("height", 768))
    n         = min(int(data.get("n", 2)), 4)
    seed      = int(data.get("seed", 42))

    if not prompt or not src_url:
        return jsonify({"error": "prompt et source_image_url requis"}), 400

    # Télécharge l'image source
    try:
        r = req_lib.get(src_url, timeout=15)
        r.raise_for_status()
        source_img = Image.open(io.BytesIO(r.content)).convert("RGB").resize((width, height))
    except Exception as e:
        return jsonify({"error": f"Impossible de télécharger l'image source : {e}"}), 400

    images_out = []
    generator  = torch.Generator("cuda").manual_seed(seed)

    for i in range(n):
        g = torch.Generator("cuda").manual_seed(seed + i)
        result = pipe(
            prompt=prompt,
            image=source_img,
            strength=0.75,
            num_inference_steps=4,
            guidance_scale=0.0,
            generator=g,
        ).images[0]

        buf = io.BytesIO()
        result.save(buf, format="WEBP", quality=90)
        b64 = base64.b64encode(buf.getvalue()).decode()
        images_out.append({
            "b64":    b64,
            "width":  width,
            "height": height,
            "seed":   seed + i,
        })

    return jsonify({"images": images_out})

# --- Démarrage ngrok + Flask ---
public_url = ngrok.connect(PORT).public_url
print(f"\n{'='*60}")
print(f"🚀  FLUX prêt !")
print(f"📋  Copie cette URL dans ton .env :")
print(f"    COLAB_FLUX_URL={public_url}")
print(f"{'='*60}\n")

# Flask dans un thread pour ne pas bloquer le notebook
t = threading.Thread(target=lambda: app.run(port=PORT, use_reloader=False))
t.daemon = True
t.start()

# Garde la cellule active
while True:
    time.sleep(60)
```

---

## Étape 3 — Créer un compte ngrok (gratuit)

ngrok permet d'exposer le notebook Colab sur une URL publique que le back office peut joindre.

1. Va sur [https://ngrok.com](https://ngrok.com) → **Sign up** (gratuit)
2. Une fois connecté, va dans **Your Authtoken** : [https://dashboard.ngrok.com/get-started/your-authtoken](https://dashboard.ngrok.com/get-started/your-authtoken)
3. Copie le token (ressemble à `2abc...xyz_...`)
4. Dans le notebook, colle-le ici :
   ```python
   NGROK_TOKEN = "2abc...xyz_..."   # ← ton token ici
   ```

---

## Étape 4 — Lancer le notebook

1. Clique **▶ Exécuter la cellule** (ou `Shift+Entrée`)
2. Attends la ligne :
   ```
   ✅ Modèle chargé !
   ```
   (première fois ~3-5 min, ensuite ~30 s depuis le cache)

3. Tu verras s'afficher :
   ```
   ============================================================
   🚀  FLUX prêt !
   📋  Copie cette URL dans ton .env :
       COLAB_FLUX_URL=https://xxxx-xx-xxx.ngrok-free.app
   ============================================================
   ```

4. **Copie cette URL** — tu en as besoin à l'étape suivante.

---

## Étape 5 — Configurer le back office

Ouvre le fichier `.env` du back office :

```
/root/tawes-platform/artifacts/admin/.env
```

Ajoute ou modifie la ligne `COLAB_FLUX_URL` :

```env
COLAB_FLUX_URL=https://xxxx-xx-xxx.ngrok-free.app
```

> ⚠️ L'URL ngrok **change à chaque redémarrage** du notebook.  
> Il faudra répéter cette étape à chaque nouvelle session Colab.

---

## Étape 6 — Redémarrer le back office

Dans le terminal du serveur :

```bash
# Tuer l'ancien process
pkill -f "artifacts/admin/src/server.js"
sleep 1

# Relancer
cd /root/tawes-platform/artifacts/admin
nohup node --env-file-if-exists=.env src/server.js > /tmp/tawes-admin.log 2>&1 &
echo "Démarré : $(cat /tmp/tawes-admin.log)"
```

Vérifie que `fluxConfigured` vaut `true` :

```bash
curl -s http://localhost:4000/api/session
# → {"authenticated":false,"vimeoConfigured":false,"fluxConfigured":true,"marginDh":10}
```

---

## Étape 7 — Utiliser le bouton dans le back office

1. Connecte-toi sur [http://localhost:4000](http://localhost:4000)
2. Va dans l'onglet **Produits**
3. Clique le bouton **Médias** d'un produit (il faut qu'il ait au moins une image)
4. Clique **✦ Personnaliser**
5. Dans le panneau qui s'ouvre :
   - **Sélectionne l'image source** à modifier (clic sur la vignette)
   - Choisis un **preset** ou tape ton propre prompt (ex: *"Fond blanc épuré, éclairage studio"*)
   - Choisis le nombre de **variantes** (1, 2 ou 4)
   - Clique **✦ Générer**
6. Après ~20-40 secondes, les variantes apparaissent
7. Clique **Ajouter aux médias** sur celles qui te conviennent

---

## Dépannage

| Symptôme | Cause | Solution |
|---|---|---|
| Bouton grisé "FLUX non configuré" | `COLAB_FLUX_URL` absent du `.env` | Étapes 4 + 5 + 6 |
| "Le notebook Colab est hors ligne" | Cellule Colab pas encore lancée ou expirée | Relancer la cellule, attendre le message `🚀 FLUX prêt !` |
| "Erreur 401 ngrok" | Token ngrok manquant ou invalide | Vérifier le token à l'étape 3 |
| Génération très lente (+2 min) | CPU runtime au lieu de GPU | Vérifier : **Modifier le type d'exécution → T4 GPU** |
| URL ngrok ne fonctionne plus | Colab déconnecté, URL expirée | Relancer le notebook → nouvelle URL → répéter étape 5 + 6 |
| "source_image_url requis" | Produit sans image | Ajouter d'abord une image via le panneau Médias |

---

## Notes importantes

- **Session Colab gratuite** : 4-12h max selon l'utilisation. Au-delà, le runtime s'arrête.
- **L'URL ngrok change** à chaque redémarrage du notebook. C'est normal.
- **Colab Pro** (10$/mois) donne des sessions plus longues et des GPU plus puissants (A100).
- Les images générées sont en **WebP 768×768**. Elles sont retournées en base64 et ne sont **pas encore sauvegardées** tant que tu ne cliques pas "Ajouter aux médias".
