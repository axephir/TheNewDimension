# The New Dimension — téléchargements

Version actuelle : **0.1.1-dev**, correctif du portail.

**[Télécharger le mod (.jar)](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/tnd-0.1.1-dev.jar)**

[Archive ZIP et instructions](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/TheNewDimension-0.1.1-dev.zip)

Minecraft Java **26.2**, NeoForge **26.2.0.93**, Java **25**.

Fermer le jeu, retirer l’ancien JAR `tnd-0.1.0-dev.jar` du dossier `mods`, puis y placer `tnd-0.1.1-dev.jar`. Conserver un seul JAR du mod. En multijoueur, mettre à jour également le serveur et les autres clients.

Le monde existant peut être conservé. Utiliser un cristal composite sur le mécanisme pour activer le portail, puis cliquer une deuxième fois pour traverser.

Le correctif charge les chunks avant de lire le terrain, tient compte de toute la plateforme et cherche une case d’arrivée libre. Les blocs existants restent préservés. [Détails et validation](PORTAL_FIX_0.1.1.md).

Le cahier des charges complet n’est pas réalisé. Cette version reste un prototype de développement.

Commandes de développement : `/tnd enter`, `/tnd leave`, `/tnd masteries`.
