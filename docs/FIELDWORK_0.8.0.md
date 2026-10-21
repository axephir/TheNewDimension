# Observatoires et expéditions — 0.8.0-dev

Minecraft Java 26.2 / NeoForge 26.2.0.93 / Java 25. Cette version relie les paysages, les ruines et l’équipement de la forge à huit objectifs personnels et à un moyen de revenir d’une longue expédition.

## Observatoire ancien

Un septième bâtiment se génère dans les six biomes de Vestige, sur la grille commune des installations : **63 × 43 × 63 blocs**, cinq volumes fermés, vestibule et atelier, nef à deux niveaux, galerie accessible par escalier, bibliothèque, dortoir et archive arrière. Un mécanisme céleste suspendu occupe la nef ; cartes stellaires, lampes, rayonnages, mobilier et deux ancres donnent un rôle aux différentes pièces.

Le pupitre central ouvre les expéditions et enregistre la découverte du site. Les cartes stellaires racontent un fragment de l’histoire en cliquant dessus. Les coffres contiennent des fournitures d’exploration, sans matrice du boss ni trophée final. Le bâtiment comporte des portes et conserve ses issues usuelles.

Les observatoires demandent **de nouveaux chunks**. La génération conserve la même grille espacée que les autres installations ; aucun bâtiment existant n’est remplacé. Les ancres et le pupitre ont de vraies données de bloc persistantes. Un pupitre fabriqué donne accès aux objectifs mais ne valide pas une découverte naturelle.

Pour préparer un test avec les permissions opérateur : `/locate structure tnd:observatory` ou `/place template tnd:observatory`, en choisissant un emplacement libre. Ces commandes ne sont pas nécessaires en survie.

## Objectifs depuis l’inventaire

Le nouvel onglet sous celui des maîtrises ouvre **Expéditions**. Aucun livre ni objet à porter. Le pupitre et `/tnd expeditions` ouvrent le même écran, accessible à tous les joueurs. Deux pages présentent les objectifs, leur progression et les récompenses ; les boutons indiquent « En cours », « Récupérer » ou « Obtenue ».

Survoler le bouton d’un objectif donne sa description et son butin. Les deux objectifs de voyage affichent aussi la liste des six biomes, avec une coche sur ceux déjà visités. Les récompenses se récupèrent manuellement, une seule fois par personnage, dans n’importe quelle dimension.

| Objectif | Condition | Objets | XP de base |
| --- | --- | --- | --- |
| Premiers relevés | Visiter 3 biomes de Vestige | 2 fragments, 4 poussières | 60 Exploration |
| Les six visages de Vestige | Visiter les 6 biomes | 4 alliages trempés, 16 poussières | 160 Exploration |
| Les machines se souviennent | Réparer 3 consoles naturelles distinctes de Vestige | 1 module de stabilisation | 120 Ingénierie |
| Récoltes de la nuit | Récolter 6 agrégats mûrs à des emplacements distincts | 8 poussières | 80 Technologies anciennes |
| Faire taire les veilleurs | Vaincre 3 sentinelles libres de Vestige | 8 poussières | 80 Technologies anciennes |
| Le ciel oublié | Utiliser le pupitre d’un observatoire naturel | 4 fragments, 1 cristal composite | 100 Exploration |
| Métal résonant | Porter une pioche ou une lame résonante | 6 poussières, 4 éclats résonants | 80 Ingénierie |
| Cartographe de Vestige | Récupérer les 7 récompenses précédentes | 1 trophée lumineux, 8 alliages trempés | 240 Exploration |

Les gardiens des épreuves ne comptent pas comme sentinelles libres. Relire une console allumée n’ajoute pas une réparation. Les consoles posées par les joueurs sont exclues. Les agrégats peuvent être cultivés, mais chaque emplacement ne compte qu’une fois pour l’objectif. Les actions créatives et le mode Spectateur n’accordent ni progression ni récompense.

Les notes sont personnelles, synchronisées uniquement à leur propriétaire, sauvegardées et conservées après la mort. Les coordonnées mémorisées sont bornées à trois consoles et six récoltes ; les compteurs saturent au but demandé. Une récompense est marquée récupérée avant de donner les objets. Un inventaire plein laisse tomber uniquement le reste du butin.

Les notes commencent avec la 0.8 : revisiter les biomes et réaliser de nouvelles réparations permet de remplir les objectifs. Un outil résonant déjà possédé en 0.7 est reconnu. Les XP et talents précédents ne sont pas réinitialisés.

## Ancre et prisme de retour

Les ancres de l’observatoire peuvent servir de point de retour. Une ancre et un prisme se fabriquent aussi après la forge ; obtenir un alliage trempé révèle leurs recettes dans le livre natif.

```text
Ancre :                 Prisme :
T C T                   · T ·
R A R                   E B E
T C T                   · T ·

T : alliage trempé ; C : cristal composite ; R : éclat résonant
A : bloc d’alliage ancien ; E : poussière d’écho ; B : boussole
```

1. Dans Vestige, tenir le prisme et cliquer sur une ancre pour lier cet objet à ce point.
2. Durant l’exploration, maintenir l’utilisation du prisme pendant **100 ticks, soit cinq secondes à vitesse normale**, en restant immobile.
3. L’arrivée réussie consomme **deux poussières d’écho** et applique **60 secondes de recharge**.

Les coordonnées sont visibles dans l’infobulle du prisme. Chaque prisme conserve sa liaison indépendamment ; il peut être partagé. Cliquer sur une autre ancre change sa destination.

Bouger de plus de 0,3 bloc, subir des dégâts, relâcher l’utilisation, changer de dimension ou se déconnecter annule la canalisation. Le retour fonctionne uniquement à l’intérieur de Vestige : il ne remplace pas les portails. Aucune poussière n’est consommée lors d’un échec.

Avant de déplacer le joueur, le serveur vérifie l’identité de l’ancre et cherche une arrivée praticable. Détruire puis reposer une ancre au même endroit invalide les anciennes liaisons. Une arrivée entièrement obstruée est refusée ; aucun bloc n’est effacé. Les règles d’arrivée partagent les contrôles des portails : sol solide, espace pour le joueur, absence de liquide, feu ou autres dangers connus et respect de la bordure.

Le prisme charge uniquement le voisinage immédiat de la destination au moment du retour, au plus quatre chunks pour cette recherche ; il ne maintient aucun chunk forcé. Un Éclat errant appartenant au joueur et en mode suivi à moins de six blocs accompagne le retour si une place sûre est disponible. Ceux qui attendent restent au départ. Un message signale un familier qui ne peut pas arriver.

Le trophée du cartographe constitue une récompense de collection lumineuse. Carreaux célestes, pierre astronomique, cartes stellaires, treillis de laiton et lampes d’observatoire se fabriquent au tailleur de pierre. Toutes les nouvelles textures restent en **16 × 16** ; le mod en compte 85.

## Validation

La compilation et les régressions du noyau vérifient 51 035 assertions. Les 15 contrôles de ressources couvrent notamment les modèles, PNG, recettes, structure NBT et traductions. Les 54 GameTests requis comprennent les 43 tests précédents et onze nouveaux scénarios : notes obtenues depuis les vrais biomes, actions nocturnes, réparations, mort confirmée des sentinelles, menu de récompenses, sauvegarde du joueur, restauration après mort, trophée fini, galerie accessible, autorité du pupitre et retours réussis ou annulés.

Le serveur GameTest omet normalement les dimensions de datapack. Les nouveaux tests créent donc un vrai niveau Vestige dans un stockage de test séparé, avec un générateur plat pour les fixtures. Le climat procédural réel reste vérifié par les tests de biomes existants. Le client simulé utilise le tick natif du joueur pour exercer les cent ticks d’utilisation du prisme, sans appeler directement sa fin de canalisation.

Les captures proviennent du client Minecraft normal. Les commandes préparent le monde de validation ; ce ne sont pas des captures d’une survie sans assistance. Les mondes, caches et réglages locaux sont exclus des fichiers publiés. L’ambiance et les 24 sons originaux des versions précédentes restent inclus. Les objectifs scénaristiques finaux et les autres filières industrielles du cahier des charges restent à développer.

Dans le client normal, un observatoire généré naturellement a été trouvé avec `/locate`, ses salles et son pupitre ont été inspectés, puis trois biomes ont été enregistrés en mode Survie. Le bouton de l’inventaire ouvre bien les notes et les deux premières récompenses testées passent à « Obtenue ». Le retour a aussi été exercé sur plus de huit kilomètres : la commande `execute unless loaded` confirme que le chunk de l’ancre était déchargé avant la canalisation. L’arrivée réussit sur l’ancre et le stock de poussière passe de 49 à 47. La sortie audio du client cloud est un périphérique nul ; les fichiers Ogg sont décodés séparément avec ffmpeg, sans validation d’écoute sur des enceintes physiques.
