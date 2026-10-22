# 0.15 — Constellations

Minecraft Java 26.2 · NeoForge 26.2.0.93 · Java 25.

## Ouvrir l’arbre

Le premier bouton à gauche de l’inventaire ouvre les maîtrises. La commande `/tnd masteries` reste disponible. Les quinze icônes du bandeau choisissent une maîtrise ; leur survol affiche son nom, son niveau, son expérience totale et ses points disponibles. Un petit point doré indique qu’il reste des points à dépenser.

L’en-tête affiche le niveau, les points et une barre d’expérience. Survoler cet en-tête donne l’expérience totale et l’expérience restante avant le prochain niveau. Les points appartiennent à la maîtrise choisie.

## Lire une constellation

Les cinq fondations et les deux spécialisations figurent dans un même arbre. Chaque liaison correspond au véritable prérequis du talent. Les icônes évoquent son usage : outils, ressources, perception, précision, graines, défense, déplacement, alchimie ou circuits.

- Un cadre doré indique un talent déjà appris ; son badge conserve le rang exact.
- Un cadre clair et une fiche « prochain rang accessible » indiquent une amélioration disponible.
- Les talents encore verrouillés restent consultables. Leur cadenas, leur palier et leur fiche expliquent les conditions manquantes.
- Une étoile distingue la capacité active en bout de branche.

Survoler un nœud affiche sa description complète, son type, ses rangs, son coût éventuel et ses conditions. Cliquer sélectionne le talent : **la sélection ne dépense aucun point**. Utiliser ensuite **Apprendre** ou **Améliorer** pour acheter un rang. Les points et les conditions restent vérifiés par le serveur. Un bref éclat et un son doux accompagnent le rang effectivement reçu.

La grande présentation conserve la fiche sélectionnée à droite. Ses textes peuvent défiler à la molette ; les boutons d’action restent à portée. Sur une petite fenêtre, l’arbre occupe toute la largeur : la fiche complète se lit au survol du nœud ou de son nom dans la barre inférieure. Le bouton d’achat reste distinct.

Une capacité active apprise possède le bouton **Utiliser**, avec affichage de sa recharge. L’écran se ferme à l’utilisation pour retrouver immédiatement le jeu. Les ingrédients et outils nécessaires figurent dans sa description. La recharge commune reste de 60 secondes. Le bouton **Percevoir** reste accessible après l’apprentissage de Perception.

## Navigation et confort

- Glisser avec le bouton gauche pour déplacer l’arbre.
- Utiliser la molette ou les boutons − et + pour zoomer autour du pointeur ou du centre.
- Le bouton de recentrage et la touche **Début** rétablissent une vue d’ensemble.
- **Page précédente / suivante**, ou la molette sur le bandeau, changent de maîtrise.
- **Tab** parcourt les commandes ; les **flèches** parcourent les nœuds depuis un nœud sélectionné. Entrée active le bouton ayant le focus.
- **Échap** ou la touche d’inventaire ferment le menu.

La dernière maîtrise et le dernier talent consultés sont retenus pendant la session client. Le fond spatial utilise des étoiles discrètes, des nappes colorées peu contrastées et une circulation lente sur les branches apprises. Les capacités et les textes restent fixes. Le bouton **~** permet de réduire les mouvements, avec la préférence d’animation TND existante. Les sons d’interface suivent le réglage audio d’interface de Minecraft ; le simple survol reste silencieux.

## Distinctions

L’onglet **Titres** présente les quatre paliers sous forme d’une constellation dédiée : niveaux 100, 250, 500 et 1 000. Sélectionner un palier explique son titre et son sceau ; **Porter ce titre** l’équipe si le niveau est atteint. Le titre porté est identifié et peut être masqué. Les distinctions restent cosmétiques et ne réinitialisent pas la progression.

## Installation

Remplacer l’ancien JAR TND sur le client et le serveur. La refonte s’applique aux personnages et parties existants : aucun nouveau chunk n’est nécessaire pour utiliser le menu. Les identifiants de sauvegarde, talents, coûts, effets, points, paliers et paquets de validation restent compatibles. Cette version conserve le contenu de la 0.14.

## Vérification

La compilation, les assertions du noyau, les contrôles de ressources bilingues et les GameTests serveur existants vérifient les régressions. Les essais du client natif utilisent une copie de monde de démonstration et des niveaux préparés pour examiner les différents états ; ils ne représentent pas une progression acquise en Survie. Le compte rendu joint à la publication et la galerie indiquent les vérifications et captures réellement effectuées.
