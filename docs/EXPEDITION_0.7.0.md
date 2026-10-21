# Expédition et industrie — 0.7.0-dev

Minecraft Java 26.2, NeoForge 26.2.0.93, Java 25. Cette étape relie le sanctuaire à une première chaîne industrielle de transformation. Les fonctionnalités de la 0.6 sont conservées.

## Le Conservateur brisé

Terminer les trois épreuves du sanctuaire, récupérer le cœur habituel, puis **tenir une poussière d’écho en main et cliquer sur le noyau central (coût : deux poussières dans l’inventaire)**. Le combat est facultatif. Il fonctionne aussi dans les sanctuaires déjà terminés en 0.5 ou 0.6, sans chercher de nouveaux chunks. L’égide et le trésor des trois ailes restent disponibles indépendamment de cette confrontation.

Le Conservateur est une nouvelle entité avec un modèle original : reliquaire suspendu, couronne de six fragments tournants, deux mains détachées à trois doigts, cœur animé. Ses trois états utilisent chacun un atlas **16 × 16**. Il ne se reproduit pas dans les biomes et n’apparaît pas spontanément.

- **Fracture** : le boss marque la position du joueur pendant deux secondes. Sortir du cercle de 2,8 blocs avant l’impact évite les sept points de dégâts. La marque reste à sa position initiale.
- **Couronne** : deux cercles fixes annoncent une attaque annulaire pendant 2,4 secondes. Le centre à moins de 2,5 blocs et l’extérieur au-delà de sept blocs sont sûrs. Seule la bande intermédiaire frappe.
- **Cœur exposé** : après chaque attaque, trois secondes permettent de riposter. Le boss reste immobile ; les coups infligent 140 % de leurs dégâts habituels. Sa coque fermée réduit les dégâts à 55 %.
- **Interruption** : infliger 18 points de dégâts avant réduction pendant une charge l’interrompt et ouvre le cœur pendant quatre secondes. Les règles natives d’invulnérabilité entre les coups restent appliquées.
- **Demi-vie** : à 60 / 120 points de vie, la couronne change de couleur et les fractures marquent deux emplacements. Les dégâts de base restent identiques.

Le clic accroupi fonctionne aussi les mains vides. Les indications sont visuelles et textuelles : tracés au sol, état du cœur, barre de vie native, messages d’esquive. Cinq sons originaux et sous-titrés accompagnent éveil, préparation, couronne, impact et défaite. Ils utilisent le volume des créatures hostiles ; l’ambiance de la 0.6 continue autour du combat.

Les attaques respectent les murs par la visibilité, ne modifient aucun bloc et n’endommagent ni familiers ni autres créatures. Les issues restent libres. Le combat ne réclame aucun chargement permanent de chunks.

## Progression partagée et sauvegarde

Une seule invocation active par sanctuaire. Une activation obstruée, sans combustible ou en difficulté Paisible ne prélève rien. L’entité et le contrôleur conservent leurs UUID et leur liaison après sauvegarde. Un rechargement interrompt la préparation en cours et laisse trois secondes avant une nouvelle attaque.

Dix secondes de simulation sans joueur vivant à moins de 54 blocs, dix secondes avec une entité manquante, le passage en Paisible ou dix minutes de combat réinitialisent seulement la tentative. Les sceaux déjà terminés restent acquis. Une disparition ne donne aucune victoire. Une nouvelle tentative coûte deux poussières. La destruction du noyau supprime son boss encore chargé ; les entités chargées ultérieurement vérifient leur liaison. Les noyaux posés par un joueur n’ont aucune autorité de récompense naturelle.

Après une mort réelle du Conservateur, cliquer sur le noyau pour obtenir **une matrice d’inertie et six alliages trempés**, une fois pour tout le groupe. Cette récupération donne 240 XP de Technologies anciennes et 120 XP d’Ingénierie au joueur qui ouvre le trésor en survie. Les instances du boss ne donnent ni butin d’entité, ni XP natif, ni XP de maîtrise de mise à mort, afin de ne pas transformer les reprises en ferme.

## Forge résonante

Recette à l’établi :

```text
A A A     A = bloc d’alliage ancien
C M C     C = cristal composite ; M = matrice d’inertie
A R A     R = redstone
```

La matrice ouvre aussi les trois nouvelles recettes dans le livre natif. La forge consomme une matière et **une poussière d’écho par cycle de 20 secondes**.

| Matière | Résultat |
| --- | --- |
| Fer brut | 2 lingots de fer |
| Cuivre brut | 2 lingots de cuivre |
| Or brut | 2 lingots d’or |
| Bloc d’alliage ancien | 1 alliage trempé |

La machine a trois emplacements physiques, un aperçu du résultat, une jauge, un état explicite, une aide au survol du bouton « ? », une face éclairée, des particules et un son discret de fin de transformation. Le raccourci Maj + clic transfère matières, combustible et production. La sortie refuse l’insertion.

Les entonnoirs vanilla fonctionnent : entrée de matière au-dessus, poussière sur les côtés, extraction de la sortie par-dessous. Un signal redstone met la forge en pause. L’état de pause est sauvegardé ; sa reprise ne convertit pas une période d’arrêt en production rétroactive.

La simulation différée utilise les ticks du serveur, bornée par la configuration `maxCatchupTicks`, les matières, le combustible et la place disponible. Le temps où le serveur est arrêté ne produit rien. Un stock bloqué ou vide n’accumule pas de travail à réaliser gratuitement après remplissage. Changer de matière efface la progression du cycle. Casser la forge restitue son inventaire physique une seule fois. Aucune XP de minage, d’artisanat ou d’ingénierie n’est accordée par les cycles automatiques.

## Équipement et objectifs

La pioche résonante utilise trois alliages trempés et deux éclats résonants dans la disposition d’une pioche. La lame utilise deux alliages et un éclat dans la disposition d’une épée.

Les deux objets ont 1 800 points de durabilité, une enchantabilité de 16 et se réparent avec l’alliage trempé à l’enclume. La pioche mine au niveau diamant à vitesse 8,5. La lame a une vitesse d’attaque de 1,6 et une attaque de base de 7,5, avant talents et enchantements. Modèles en main, textures 16 × 16 et catégories d’outils natives permettent les enchantements habituels.

Un onglet de cinq progrès natifs, traduit en français et anglais, suit l’entrée dans Vestige, le trésor du sanctuaire, la matrice, la construction de la forge et l’alliage trempé. Les objectifs n’ajoutent pas une seconde récompense d’XP. La trame scénaristique finale, d’autres boss et les autres filières industrielles du cahier des charges restent à développer.

## Validation

Les vérifications de cette version comprennent la compilation Java 25, 50 114 assertions du noyau, 14 vérifications de ressources et PNG, le décodage des 24 fichiers Vorbis et 43 GameTests natifs requis (42 TND et un vanilla). Les nouveaux scénarios exercent dégâts, esquive, abri de la couronne, seconde phase, interruption, invocabilité d’un ancien sanctuaire, obstruction, sauvegarde, reprise, récompense partagée, consommation de la forge, rattrapage après absence, pause persistée, inventaire et transfert par de vrais entonnoirs.

Les captures de la galerie proviennent du client Minecraft normal dans un monde de validation. Les commandes servent à préparer des conditions reproductibles ; les combats et interfaces sont ceux du mod. L’environnement cloud utilise une sortie audio nulle : les fichiers sont décodés et mesurés, les sous-titres vérifiés à l’écran ; le montage audio téléchargeable permet leur écoute sur un appareil audio.
