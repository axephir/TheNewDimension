# Éveil des profondeurs — 0.9.0-dev

Minecraft Java **26.2**, NeoForge **26.2.0.93**, Java **25**. Cette version étend la progression de Vestige avec deux grandes installations, deux espèces, deux minerais, neuf équipements et deux machines. Elle ajoute 30 blocs, 16 sons originaux et un guide intégré. Les fonctions des versions 0.2 à 0.8 restent incluses.

## Prendre ses repères

Les trois onglets sont placés **à gauche de l’inventaire**, pour libérer l’affichage des effets de potion à droite. Ils suivent aussi l’ouverture du livre de recettes ; sur un écran étroit où le livre recouvre l’inventaire, ils attendent sa fermeture. Dans l’inventaire, le troisième onglet **?**, sous Maîtrises et Expéditions, ouvre un guide de six pages : voyage, combat, matériaux, machines, expéditions et retour. Il explique les recettes importantes, les attaques et les entrées des machines sans objet à transporter ni permissions opérateur.

Une petite carte de découverte nomme le biome lors d’un changement dans Vestige. Viser une créature du mod à moins de seize blocs, avec une ligne de vue dégagée, affiche sa santé et signale la préparation ou la fenêtre de récupération. Les barres de progression des maîtrises, objectifs, extracteurs et ateliers ont une interpolation visuelle ; les chiffres et les récompenses restent déterminés par le serveur. Un accent lumineux discret anime les panneaux. Les réglages client `interface.immersiveHud` et `interface.animations` permettent de désactiver ces ajouts et de réduire les mouvements.

Les sentinelles compriment leur corps et ouvrent leurs appuis pendant la charge ; le familier anime ses antennes et sa respiration ; le Conservateur arme ses bras et contracte sa couronne. Les contrôleurs de portail actifs émettent une petite spirale. Les nouveaux ennemis disposent de modèles articulés propres, avec démarche, respiration, suivi de tête, préparation et récupération.

## Deux grandes installations

| Site | Dimensions | Contenu |
| --- | --- | --- |
| Citadelle auréole | 95 × 47 × 95 | Neuf volumes fermés, grande nef à colonnes, galerie et escalier, deux armureries, bibliothèque, casernement avec couchettes, cuisine, ateliers, archive, reliquaire suspendu, coffres et ancres. |
| Conservatoire d’écho | 79 × 45 × 79 | Verrières à nervures, nef centrale, deux jardins minéraux, galerie accessible, orgue cristallin, atelier, archive, pupitre, carillon et ancres. |

Les sept structures précédentes reçoivent aussi une passe de détail : pierre sculptée, joints fissurés, corniches, incrustations et variantes d’éclairage. Les collisions, conteneurs, passages et positions des épreuves sont conservés. Le carillon harmonique se joue par clic, avec une courte recharge pour éviter les sons superposés.

Les sept types de Vestige utilisent une grille commune espacée. Les **neuf structures**, y compris l’avant-poste initial et le sanctuaire du portail de l’Overworld, sont maintenant placées au sommet d’un **massif plein de scorie ferrenoire** : roche noire à reflets métalliques, filons discrets, aiguilles sur les flancs et couronne de grands pics autour du bâtiment central. Une montée pavée de cinq blocs de large serpente jusqu’à l’entrée. Le plus grand massif atteint environ 215 blocs de diamètre ; le sommet est surélevé d’au moins 48 blocs au-dessus du pied de la montée. La hauteur du terrain et la place disponible pour le bâtiment entier sont vérifiées avant génération. Les écritures sont limitées au chunk en cours, et le massif, sa rotation et son escalier sont sauvegardés avec la structure. Les coffres offrent du minerai brut et des fournitures ; le cœur lithique vient du combat. Un pupitre de citadelle ou de conservatoire ouvre les notes ; seul celui d’un véritable observatoire valide l’objectif de la 0.8.

**Explorer de nouveaux chunks** pour les nouveaux sites et les minerais. Les anciens bâtiments déjà enregistrés conservent leur forme. Pour préparer un test opérateur : `/locate structure tnd:aureate_citadel` et `/locate structure tnd:echo_conservatory`. `/place structure tnd:aureate_citadel` permet de tester le bâtiment avec son massif dans un emplacement libre ; `/place template` ne place que le bâtiment.

## Rencontres et butins

**Lithogarde** — grand construct de 3,4 blocs de haut, 72 PV et armure naturelle. Rare la nuit, au plus un de son espèce dans son voisinage immédiat lors des tentatives de génération. Le jour, il reste dormant sauf provocation. Il annonce ses zones pendant **deux secondes**, puis frappe : une zone de rayon 3,5 blocs, alternant avec trois fractures plus étroites. Une frappe touche pour huit dégâts avant protection. Quitter les marques permet d’esquiver ; la récupération laisse une ouverture. Butin d’une mort attribuée au joueur : un cœur lithique et de l’auralite brute.

**Traqueur de verre** — prédateur de 30 PV des forêts de verre, corniches creuses et champs fracturés. Il annonce trois petites zones pendant **1,6 seconde**, puis les frappe en salve. Une aiguille inflige quatre dégâts avant protection. Un coup réussi d’au moins trois dégâts pendant la préparation l’interrompt et impose une récupération. Butin : filaments de verre et cristaux de noctilite.

Les deux espèces sont réservées à Vestige pour leur apparition naturelle, excluent le mode Paisible et vérifient le sol ainsi que l’éclairage. Leurs attaques spéciales respectent les lignes de vue et épargnent les familiers, les joueurs créatifs et les spectateurs. Elles ne cassent aucun bloc. Les marques sont figées au début de la préparation ; elles ne suivent pas les mouvements du joueur.

## Matériaux et équipements

Les petits filons restent **exposés sur les sols** de Vestige et demandent une pioche en fer ou mieux. Leur génération reste dans le chunk décoré et remplace uniquement un sol naturel dégagé.

- **Auralite dorée** : étendues silencieuses, champs fracturés et dunes vitrifiées. Cuire le minerai brut donne un lingot.
- **Noctilite violette** : forêts de verre, bassins de cendre et corniches creuses. Cuire le minerai brut donne un cristal.

Silk Touch conserve le bloc ; Fortune s’applique aux fragments bruts. Les minerais participent à la maîtrise de minage, à la perception et au suivi des blocs reposés. Les lingots d’auralite réparent leurs outils ; l’alliage ombral répare son équipement à l’enclume.

| Équipement | Particularités |
| --- | --- |
| Pioche et lame d’auralite | 1 250 points de durabilité ; matériau rapide, vitesse de minage 10, enchantabilité 20. |
| Pioche et lame ombrales | 2 250 points de durabilité ; vitesse de minage 9 et résistance accrue. |
| Casque, plastron, jambières et bottes ombraux | Ensemble complet : 20 points d’armure, huit de robustesse et 20 % de résistance au recul. Textures de l’armure portée originales. |
| Marteau de gravité | Arme lourde. Maintenir l’utilisation une seconde au sol déclenche une onde frontale à cinq blocs : au plus quatre monstres, six dégâts avant protection, recul, trois points de durabilité et six secondes de recharge. L’onde ne touche pas les joueurs et ne casse aucun bloc. |

L’alliage ombral relie la nouvelle exploration à la forge et aux combats :

```text
N T N     N : cristal de noctilite
T F T     T : alliage trempé de la forge
N T N     F : filament de Traqueur de verre

Résultat : 4 alliages ombraux.
```

Les quatre pièces d’armure utilisent les formes usuelles des recettes Minecraft. Les nouvelles pioches et lames suivent aussi leurs formes habituelles. Le marteau demande trois alliages ombraux en haut, un cœur lithique au centre et un bâton en bas. Les outils ombraux et le marteau comptent également pour l’objectif d’équipement des expéditions.

## Deux machines et une forge étendue

**Séparateur de minerais** : un minerai brut d’auralite ou de noctilite et une poussière d’écho produisent **deux granulés en douze secondes**. Chaque granulé se cuit en une unité de métal ou de cristal. La forge les accepte également, à raison d’une poussière par opération. Les lingots, cristaux et granulés ne peuvent pas être réintroduits dans le séparateur pour multiplier indéfiniment le résultat.

```text
T A T     T : alliage trempé
R H R     A : lingot d’auralite ; R : éclat résonant
T A T     H : haut fourneau
```

Les entonnoirs insèrent le minerai par le dessus, la poussière par les côtés et récupèrent la sortie par dessous. Un signal redstone suspend le traitement. Le séparateur partage les contrôles de la forge : matière et combustible physiques, vérification de toute la place nécessaire, sauvegarde, temps différé borné et absence de production rétroactive avec un combustible ajouté tardivement.

**Récolteur d’écho** : placer des agrégats autour de la machine dans un carré de **5 × 5**, à la même hauteur. Il attend la nuit de Vestige et un agrégat réellement mûr, puis récolte **une poussière toutes les dix secondes** en remettant cet agrégat à zéro. Un charbon ou charbon de bois donne huit récoltes. Il n’accorde ni XP ni progrès d’expédition automatiques.

```text
T F T     T : alliage trempé ; F : filament de verre
C H C     C : cristal composite ; H : cœur lithique
T A T     A : lingot d’auralite
```

Combustible par-dessus ou sur les côtés ; sortie par dessous. Une sortie pleine conserve l’agrégat mûr. La redstone met en pause. Les charges de combustible restantes sont sauvegardées. La machine travaille uniquement dans un chunk actif : elle ne force pas les voisins et ne simule pas des récoltes durant l’absence du joueur. Chaque agrégat doit repousser naturellement avant une nouvelle récolte.

La forge précédente accepte désormais aussi les minerais bruts et granulés nouveaux. Ses quatre transformations précédentes restent disponibles. Quatre progrès natifs accompagnent la découverte des minerais, le plastron, le marteau et le récolteur.

## Construction et son

Les **30 nouveaux blocs** comprennent deux minerais, trois machines ou accessoires, 19 matériaux et décorations, et six variantes d’escaliers/dalles : maçonnerie de citadelle, piliers et corniches dorés, carreaux ombraux, pierre gravée, mosaïque, panneaux vert-de-gris, alliage ajouré, lampes et effigie lithique. Les variantes décoratives ordinaires sont disponibles au tailleur de pierre ; blocs de stockage et effigie utilisent leurs matériaux. Les **135 textures** comprennent 122 textures de blocs et d’icônes en **16 × 16**, ainsi que 13 atlas de créatures et d’armure en **64 × 64**. Les cinq espèces ont des silhouettes enrichies : carapaces, optiques, côtes, mandibules, phalanges, articulations ou couronnes selon la créature. L’armure ombrale a un casque à visière ouverte, une crête, des joues, des épaulières, des brassards, un plastron en relief, des tassettes, des genouillères et des grèves. Les pièces suivent les poses natives, y compris accroupissement et monture. La scorie ferrenoire se taille depuis la pierre cendrée ; sa variante veinée demande une poussière d’écho. Leur lumière reste discrète (niveaux 1 et 4).

Les **16 nouveaux sons** couvrent réveil, pas, préparations, impacts, chutes, machines, marteau, carillon, accomplissement d’objectif et rappel. Les 24 sons précédents sont conservés : 40 sons originaux au total, avec sous-titres français et anglais. Les attaques utilisent la catégorie Créatures hostiles ; les ateliers et le carillon suivent Blocs ; les effets du joueur suivent Joueurs. Les ambiances restent réglables avec le curseur Ambiance et les options du mod.

## Vérifications et limites

Les 74 GameTests requis comprennent les 54 scénarios précédents et 20 nouveaux cas : vrais minerais par habitat, cuisson native, attributs de l’armure portée, dégâts et esquive, alternance des frappes, interruption, tables de butin, utilisation native du marteau, traitement et entonnoirs, récolte nocturne, gardes de fonctionnement, sauvegarde et trajets aux galeries. Les recherches de chemin des grands sites utilisent un budget augmenté pour explorer le détour par leurs escaliers ; la citadelle utilise aussi un point de passage sur le palier, avec une recherche native valide pour chacun des deux tronçons. Le sujet de ce contrôle de trajet est immobilisé pour que ses objectifs d’IA aléatoires ne modifient pas sa position pendant la mesure. Trois scénarios supplémentaires vérifient la génération et la sauvegarde des neuf massifs, les plafonds du véritable terrain de Vestige, les limites d’écriture par chunk, les fondations, la couronne, l’accès complet par la navigation native et l’avant-poste initial de l’Overworld. Les 18 contrôles de ressources vérifient aussi les parents de progrès, les recettes, modèles, traductions, NBT, PNG et atlas d’armure. Le noyau existant conserve ses 51 035 assertions.

Les fixtures de récolte utilisent le niveau Vestige isolé du serveur GameTest de la 0.8 ; les autres tests restent dans son monde jetable. Les fichiers audio sont décodés avec ffmpeg et contrôlés en durée, niveau et absence de saturation. Le client cloud utilise un périphérique audio nul : les captures visuelles et la vidéo ne constituent pas une écoute sur des enceintes physiques. Les mondes de vérification sont préparés avec des commandes, sans prétendre représenter une survie sans assistance.

Le scénario final, les catastrophes réversibles et d’autres filières industrielles restent à développer pour la suite du projet. La 0.9 conserve les identifiants de sauvegarde et les récompenses déjà récupérées ; elle ne réinitialise pas les maîtrises ni les notes de la 0.8.
