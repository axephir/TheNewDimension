# Sanctuaire de résonance — 0.5.0-dev

Minecraft Java 26.2 / NeoForge 26.2.0.93 / Java 25. Cette version ajoute un premier donjon jouable à Vestige et conserve les fonctionnalités de la 0.4.

## Trouver le sanctuaire

Le sanctuaire de résonance se génère dans les trois biomes de Vestige, sur la même grille espacée que les relais, archives et fonderies. Cela évite de superposer des grilles indépendantes. Les anciens bâtiments et les constructions des joueurs ne sont pas remplacés : la génération naturelle du sanctuaire demande de nouveaux chunks.

L'installation occupe un volume de 63 × 43 × 63 blocs. Elle comporte cinq volumes fermés et reliés : vestibule avec atelier et réserves, nef centrale avec mécanisme suspendu, deux ailes latérales et chambre arrière. Piliers, contreforts, toits, fenêtres, conduites, cabinets et éclairage utilisent les matériaux du mod. La fresque du vestibule explique les épreuves. Des lampes bleues, vertes et rouges au sol distinguent les trois sceaux, même lorsque la structure est tournée.

Pour la validation en créatif ou avec les permissions opérateur : `/locate structure tnd:resonance_vault`. La commande `/place template tnd:resonance_vault` permet aussi de placer le véritable modèle, en choisissant au préalable un emplacement libre. Ces commandes ne sont pas nécessaires en survie.

## Trois épreuves, sans enfermer le joueur

1. Cliquer sur le sceau de l'aile bleue avec une poussière d'écho dans l'inventaire. Un gardien s'éveille.
2. Après sa défaite, alimenter le sceau vert. Deux gardiens protègent cette aile.
3. Alimenter le sceau rouge, dans la chambre du fond. Trois gardiens constituent la dernière épreuve.
4. Revenir au noyau de la nef pour récupérer le trésor commun.

Chaque activation en survie coûte une poussière. L'activation dans le mauvais ordre, sans combustible, en mode Paisible ou sur des positions d'apparition obstruées ne consomme rien. Aucun bloc n'est supprimé pour créer les gardiens. Les activations répétées ne créent pas de groupes supplémentaires.

Les gardiens sont les sentinelles de faille de la 0.4, avec leurs attaques annoncées pendant 1,6 seconde. Sortir du cercle ou frapper pendant la charge permet d'éviter l'impact. Ils fonctionnent aussi le jour dans l'installation. Leurs points de vie et dégâts restent ceux de l'espèce ; la difficulté augmente par le nombre et le placement. Une barre native indique l'épreuve et les gardiens encore présents.

Les corridors restent ouverts. Aucun mur n'est placé derrière le joueur. Il est possible de fuir, d'utiliser son équipement habituel et de revenir avec des amis. Les sceaux et le noyau sont partagés par le groupe.

## Sauvegarde et reprise

Les ailes terminées restent validées après sauvegarde. Le noyau et les gardiens conservent les UUID du groupe en cours : un rechargement ne crée pas de nouvelle vague. Seule une mort réellement acceptée par le moteur compte comme victoire. La disparition ou le déchargement d'une entité ne valide jamais une aile.

Une épreuve est abandonnée après dix secondes de simulation sans joueur vivant dans un rayon de 54 blocs autour du noyau, après dix secondes avec un gardien introuvable, après cinq minutes de simulation, ou lors du passage en mode Paisible. Les gardiens restants sont supprimés. Les ailes déjà validées restent acquises ; recommencer l'aile interrompue demande une nouvelle poussière. Les temps ne s'écoulent pas lorsque le contrôleur n'est pas simulé.

La destruction du noyau supprime les gardiens chargés ; les gardiens déchargés vérifient leur liaison lorsque leur zone et le noyau redeviennent accessibles. Un noyau miné puis reposé est une décoration sans autorité d'épreuve. Le système ne force aucun chunk et ne régénère pas les constructions détruites par les joueurs.

Les gardiens d'épreuve ne donnent ni butin d'entité, ni XP vanilla, ni prime de maîtrise de mise à mort. Cela empêche de récolter les récompenses des sentinelles en abandonnant puis relançant une aile. Les actions manuelles de défense conservent les règles habituelles des maîtrises.

## Trésor et égide

Chaque sanctuaire offre une seule récompense commune : un cœur de résonance, quatre fragments de relique et huit poussières d'écho. Le joueur qui la récupère reçoit aussi 120 XP de technologies anciennes et 80 XP d'exploration, soumis au multiplicateur serveur habituel. Le drapeau de récupération est sauvegardé : un autre joueur ou un rechargement ne donne pas une deuxième récompense. Les objets qui ne tiennent pas dans l'inventaire tombent normalement au sol.

Le cœur, deux cristaux composites et deux lingots de fer fabriquent une **égide résonante**. Utilisée vers l'air, elle consomme une poussière d'écho et accorde deux cœurs d'absorption natifs pendant quinze secondes. La recharge native est d'une minute et s'applique à toutes les égides du même joueur. L'objet fonctionne dans les dimensions vanilla et Vestige. Une absorption déjà présente est préservée, sans consommer de poussière ni déclencher de recharge.

Les objets et blocs supplémentaires ont leurs propres textures 16 × 16 et des traductions françaises et anglaises. Les descriptions des objets et la fresque expliquent les coûts et limites.

## Validation

La validation comprend la compilation Java 25, **50 114 assertions du domaine**, **12 contrôles de ressources** (dont 61 textures, toutes en 16 × 16) et **30 GameTests Minecraft requis**. Les nouveaux tests placent le véritable sanctuaire, exécutent les trois épreuves avec de vrais gardiens et dégâts, vérifient la récompense partagée unique, la sauvegarde native, la reprise d'une épreuve interrompue, les apparitions obstruées, le contrôleur reposé et les rotations/miroirs du modèle. L'égide est testée avec de vrais dégâts et l'effet d'absorption natif.

Il s'agit du premier sanctuaire de combat. Les boss originaux, les grands donjons à plusieurs étages, l'industrie supplémentaire et la conclusion narrative restent à développer. Aucun événement destructeur du scénario final n'est ajouté.
