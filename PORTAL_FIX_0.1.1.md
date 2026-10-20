# Correctif du portail — 0.1.1-dev

## Problème reproduit

L’activation affichait « The arrival site is obstructed » sur des destinations pourtant utilisables. Le code lisait la hauteur avant de charger les chunks ; Minecraft renvoie alors la hauteur minimale du monde. Il ne tenait ensuite compte que de la colonne centrale pour une plateforme de 7 × 7 blocs. Une pente voisine pouvait donc rendre la position proposée invalide à chaque tentative.

Un second défaut empêchait le transport si l’espace au-dessus du contrôleur était occupé, même lorsqu’une autre case de la plateforme était sûre.

## Correction

- Chargement des chunks de la plateforme avant la lecture des hauteurs, sans ticket permanent.
- Calcul de l’altitude sur les 49 colonnes, puis installation au-dessus du relief et des blocs existants.
- Respect des limites de construction et de la bordure du monde ; aucun effacement de blocs pour forcer l’installation.
- Recherche d’une arrivée voisine avec un sol stable, une place suffisante pour l’entité, sans fluide, danger ni collision.
- Si toute la zone est occupée, la traversée reste refusée. Le message apparaît dans la barre d’action afin de ne plus remplir le chat.
- Aucun changement d’identifiant de bloc ni de format de sauvegarde des liens existants.

## Validation exécutée

`python3 scripts/check_resources.py` : 8 contrôles réussis.

`python3 scripts/cloud_gradle.py build runGameTestServer` : compilation et régressions du noyau réussies ; le serveur Minecraft termine 8 GameTests sans échec (7 tests du mod et le test natif inclus par le moteur).

Les cas de relief irrégulier et d’arrivée centrale obstruée échouaient avant la correction et passent après. Les tests vérifient aussi le refus d’une plateforme inondée ou entièrement bouchée, la conservation des blocs existants et du cristal en cas d’échec, une seule consommation lors de l’activation, le rechargement des deux contrôleurs depuis leurs données Minecraft, et un vrai aller-retour d’un joueur serveur.

Le serveur GameTest utilise un preset qui exclut les dimensions ajoutées par datapack. Le test d’aller-retour utilise donc l’End comme seconde dimension, avec le même mécanisme de liaison que le jeu normal. Le test de dimension valide séparément les données et le générateur de Vestige. Cela ne remplace pas une validation exhaustive sur toutes les seeds ou une simulation d’arrêt brutal du serveur.

## Installer

Fermer Minecraft et, en multijoueur, le serveur. Remplacer `tnd-0.1.0-dev.jar` par `tnd-0.1.1-dev.jar` dans `mods` ; conserver un seul JAR du mod. Tous les participants et le serveur doivent utiliser la même version.

Le monde existant peut être conservé. Utiliser le cristal composite sur le mécanisme pour l’activer, puis cliquer de nouveau sur le mécanisme pour traverser. Le prototype n’a pas encore de surface de portail traversable automatiquement.
