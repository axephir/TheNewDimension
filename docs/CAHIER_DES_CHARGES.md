# CAHIER DES CHARGES — PROJET DE MOD MINECRAFT

**Version :** 0.1 — Document fondateur  
**Date :** 20 octobre 2026  
**Nom du projet :** à définir  
**Nature :** mod Minecraft original, RPG, exploration, automatisation, science-fiction et horreur atmosphérique  
**Destinataire :** Claude, assistant chargé du développement initial

---

# 1. Présentation générale

## 1.1. Vision du projet

Nous souhaitons développer un mod Minecraft original, ambitieux et évolutif, qui enrichit considérablement l'expérience de jeu sans dénaturer l'identité de Minecraft.

Le mod doit combiner :

- Une nouvelle dimension entièrement originale.
- Une civilisation ancienne technologiquement incompréhensible.
- Une progression RPG avec niveaux et arbres de compétences.
- Des systèmes avancés d'automatisation des ressources.
- Des donjons, structures et installations à explorer.
- Des créatures rares pouvant notamment devenir des familiers.
- Une difficulté supérieure au jeu classique.
- Une ambiance de science-fiction abandonnée et d'horreur psychologique.
- Une histoire environnementale, racontée principalement par l'exploration.
- Une conclusion scénaristique marquante impliquant le monde du joueur.

Le joueur doit pouvoir vivre une aventure très longue, construire des infrastructures complexes, développer progressivement ses capacités et devenir extrêmement puissant.

**Principe fondamental : le mod doit enrichir Minecraft, et non remplacer Minecraft.**

L'Overworld, le Nether et l'End doivent rester jouables normalement. Le mod doit y apporter de nouvelles possibilités sans empêcher les activités habituelles.

## 1.2. Ambition et distribution

Le projet est initialement destiné à une utilisation personnelle et multijoueur entre amis.

Une publication sur CurseForge ou Modrinth pourra être envisagée ultérieurement si le résultat atteint une qualité satisfaisante.

Il faut donc concevoir une architecture compatible avec un futur développement public : maintenable, modulaire, configurable, performante et adaptée aux serveurs dédiés.

Nous souhaitons développer notre propre mod, avec nos propres systèmes. Nous ne voulons pas dépendre structurellement d'un ensemble de mods tiers qui limiterait notre liberté créative.

L'utilisation de bibliothèques techniques appropriées reste autorisée.

## 1.3. Philosophie de conception

Le gameplay doit reposer sur quatre piliers complémentaires.

**Exploration :** découvrir des lieux, comprendre des mécanismes inconnus, trouver des ressources et affronter des dangers.

**Progression :** améliorer ses compétences, débloquer des talents, augmenter ses rendements et accéder à de nouvelles possibilités.

**Automatisation :** concevoir des installations capables de récolter, produire et transformer des ressources pendant que le joueur effectue d'autres activités.

**Mystère :** comprendre progressivement l'histoire d'une civilisation qui semble avoir disparu, sans dévoiler immédiatement la véritable nature de son existence.

La progression doit procurer une satisfaction croissante. Le joueur doit sentir qu'après plusieurs dizaines d'heures, il accomplit des choses qui lui auraient demandé énormément d'efforts au début.

Cependant, cette puissance doit être acquise progressivement, et non accordée artificiellement.

---

# 2. La nouvelle dimension

## 2.1. Concept général

La majeure partie du contenu original du mod doit être concentrée dans une nouvelle dimension.

Cette dimension constitue le cœur du projet.

Elle n'est toutefois pas destinée à devenir une prison ou un lieu de résidence obligatoire.

Le joueur peut rapporter les ressources, machines, équipements et familiers dans l'Overworld et, lorsque cela est techniquement compatible, dans le Nether et l'End.

**Distinction importante :** les ressources sont principalement obtenues dans la nouvelle dimension, mais les objets fabriqués grâce à ces ressources peuvent généralement être utilisés ailleurs.

Par exemple, le joueur doit pouvoir installer une machine minière automatisée dans sa base de l'Overworld, même si sa fabrication nécessite des composants provenant de la nouvelle dimension.

## 2.2. Direction artistique

L'environnement doit évoquer une civilisation futuriste ayant atteint un niveau technologique très supérieur à celui de l'humanité contemporaine.

Les références d'ambiance sont :

- Une science-fiction lointaine, presque incompréhensible.
- Une esthétique cyberpunk délabrée, mais sans reprendre systématiquement les codes des villes néon.
- Les vestiges de la Première Civilisation de l'univers Assassin's Creed, notamment son caractère mystérieux.
- Des technologies qui semblent parfois défier les lois physiques.
- Une architecture qui pourrait provenir d'une civilisation humaine de l'an 3500 ou d'une époque encore plus avancée.

Il ne faut pas simplement créer des vaisseaux spatiaux, des gratte-ciel futuristes ou des laboratoires conventionnels.

L'architecture doit donner l'impression que ses concepteurs percevaient l'espace et la matière différemment de nous.

Exemples de pistes artistiques non encore validées :

- Structures asymétriques qui semblent tenir sans support.
- Bâtiments composés de volumes imbriqués de façon étrange.
- Matériaux à reflets ou motifs anormaux.
- Machines dont les composants paraissent déconnectés.
- Structures partiellement enterrées ou englouties.
- Installations gigantesques dont l'utilité reste mystérieuse.
- Fragments architecturaux donnant l'impression qu'une partie du bâtiment manque dans notre espace tridimensionnel.

Ces propositions constituent une direction créative, pas une liste définitive de structures à développer.

## 2.3. Ambiance

La dimension doit sembler morte.

Lorsqu'un joueur y pénètre pour la première fois, il doit avoir l'impression d'arriver dans un endroit où une civilisation entière s'est éteinte.

L'environnement doit être silencieux, oppressant, désolé et légèrement malsain.

Il ne faut pas remplir les paysages de créatures ou de personnages.

Le vide est une composante essentielle de l'identité de la dimension.

Le joueur doit pouvoir parcourir de longues distances en ne rencontrant presque rien de vivant.

Les sons ambiants, les structures abandonnées, les machines encore alimentées et les anomalies visuelles doivent contribuer à cette sensation d'isolement.

## 2.4. Génération procédurale

La dimension doit posséder une génération de terrain originale, avec des reliefs, matériaux, formations géologiques et structures propres.

Elle ne doit pas être une simple copie de l'Overworld avec de nouvelles textures.

Les caractéristiques précises des biomes restent à concevoir.

Le système devra permettre d'ajouter progressivement :

- Des biomes.
- Des minerais.
- Des ruines.
- Des donjons.
- Des installations technologiques.
- Des créatures.
- Des phénomènes environnementaux.

Il est important de prévoir dès le départ un système de génération extensible.

---

# 3. L'ancienne civilisation

## 3.1. Véritable nature de sa disparition

La civilisation qui occupait autrefois cette dimension n'est pas morte.

Elle a volontairement quitté l'existence limitée à trois dimensions spatiales pour accéder à une quatrième dimension spatiale.

Il ne s'agit pas d'une quatrième dimension Minecraft classique, ni d'un univers parallèle.

Il s'agit d'une dimension spatiale supplémentaire, inaccessible à la perception ordinaire du joueur.

Cette idée relève de la science-fiction inspirée de concepts mathématiques.

## 3.2. La transition

La transition vers la quatrième dimension spatiale a été volontaire et progressive.

Elle s'est étalée sur une période importante.

Les habitants n'ont donc pas tous quitté leur monde simultanément.

Au moment où le joueur découvre la dimension, cette transition est achevée depuis très longtemps.

Tous les membres de cette civilisation ont déjà abandonné leur ancienne existence tridimensionnelle.

Leurs infrastructures, leurs machines et leurs bâtiments sont restés derrière eux.

## 3.3. Relation avec le joueur

Les anciens habitants sont toujours présents, mais dans une forme d'existence que le joueur ne peut normalement pas percevoir.

Ils peuvent observer les êtres tridimensionnels et potentiellement interagir avec leur environnement.

En revanche, le joueur ne peut pas directement les observer ou interagir avec eux dans des conditions normales.

Cette asymétrie doit devenir une source majeure de mystère et d'horreur.

Au début, le joueur ne doit absolument pas comprendre que les habitants sont encore là.

Il doit croire à la disparition ou à l'extinction de la civilisation.

La vérité sera révélée progressivement.

## 3.4. Vestiges technologiques

Certaines machines fonctionnent encore après des siècles ou des millénaires d'abandon.

D'autres sont presque opérationnelles.

Il doit être possible de découvrir une installation apparemment abandonnée et de la remettre en fonctionnement grâce à une intervention simple.

Exemple : une immense machine ancienne dont tous les composants sont intacts, mais à laquelle il manque seulement un module d'alimentation.

Le joueur récupère ou fabrique ce module, l'insère, et réactive l'installation.

Cette mécanique doit être récurrente dans l'exploration.

Elle permettra de rendre les ruines interactives plutôt que simplement décoratives.

---

# 4. Horreur environnementale et anomalies

## 4.1. Type d'horreur recherché

Nous ne voulons pas construire un mod d'horreur reposant essentiellement sur des monstres qui poursuivent le joueur ou des jumpscares.

L'horreur doit principalement être psychologique, environnementale et liée à l'existence de la civilisation quadridimensionnelle.

Le joueur doit parfois avoir l'impression d'être observé ou de ne pas être seul.

Cette impression doit être créée sans confirmer immédiatement ses soupçons.

## 4.2. Exemples d'événements

Plusieurs idées ont été envisagées :

- Une porte précédemment ouverte est mystérieusement fermée au retour du joueur.
- Un mécanisme change de position sans intervention visible.
- Une silhouette apparaît pendant une fraction de seconde.
- Une installation semble s'activer en présence du joueur.
- Des bruits proviennent d'une structure pourtant vide.
- Un phénomène anormal se produit, puis ne se reproduit plus pendant très longtemps.

La rareté des événements est essentielle.

Il faut éviter qu'ils deviennent prévisibles ou qu'ils se produisent toutes les quelques minutes.

L'objectif est de susciter le doute, pas de transformer la dimension en attraction horrifique.

## 4.3. Lien avec le lore

Les anomalies ne doivent pas être des événements surnaturels arbitraires.

Elles doivent pouvoir s'expliquer, au moins partiellement, par l'interaction entre notre espace tridimensionnel et les êtres capables d'évoluer dans une quatrième dimension spatiale.

Le joueur ne comprendra ce lien que progressivement.

---

# 5. Cycle jour-nuit de la dimension

## 5.1. Principe

La nouvelle dimension possède un véritable cycle jour-nuit.

Nous avons envisagé une durée totale de 40 minutes réelles :

- 20 minutes de jour.
- 20 minutes de nuit.

Cette durée constitue un réglage provisoire qui devra être testé.

## 5.2. Différences entre le jour et la nuit

Le cycle ne doit pas uniquement modifier la luminosité.

Il doit influencer directement les possibilités d'exploration et de récolte.

Certaines ressources doivent être accessibles ou exploitables uniquement pendant certaines phases.

Certaines installations peuvent fonctionner de jour, d'autres de nuit.

Des événements particuliers doivent être associés à chaque période.

### Exemples diurnes

Certaines machines captent une énergie spécifique présente pendant le jour.

Des ressources peuvent être plus facilement récoltées.

Certains mécanismes sont stabilisés par les conditions diurnes.

### Exemples nocturnes

Des structures anciennes émettent des lueurs inhabituelles.

Certaines installations se réactivent.

Des ressources changent de propriétés ou deviennent exploitables.

Des phénomènes spatiaux ou énergétiques apparaissent.

Certaines zones deviennent plus dangereuses, mais aussi plus rentables.

## 5.3. Manipulation du cycle

Un objet ou dispositif capable de manipuler le cycle jour-nuit a été évoqué.

Il pourrait permettre au joueur avancé d'influencer les conditions temporelles de la dimension.

Son fonctionnement, son coût, ses limites et son stade de déblocage restent à définir.

Il faut prévoir cette possibilité dans la conception, sans l'implémenter immédiatement.

---

# 6. Système RPG : maîtrises et compétences

## 6.1. Philosophie

Nous souhaitons un système de progression lié aux activités du joueur.

Nous préférons parler de **maîtrises** plutôt que de métiers.

Chaque grande catégorie d'actions doit disposer de sa propre progression.

Par exemple, miner augmente la maîtrise du minage, pêcher augmente la maîtrise de la pêche et construire des machines contribue à la maîtrise de l'ingénierie.

Le système doit être utilisable dans toutes les dimensions.

## 6.2. Les quinze maîtrises initiales

| Maîtrise | Activités principales |
|---|---|
| Minage | Extraction des minerais et matériaux |
| Bûcheronnage | Récolte de bois |
| Agriculture | Culture et récolte |
| Pêche | Capture de poissons et ressources aquatiques |
| Chasse | Activités de chasse |
| Élevage | Gestion et reproduction des animaux |
| Combat rapproché | Utilisation d'armes de mêlée |
| Combat à distance | Utilisation d'armes à distance |
| Défense | Protection et résistance en combat |
| Artisanat | Fabrication et transformation d'objets |
| Construction | Placement de blocs et réalisation de structures |
| Exploration | Découverte de lieux et territoires |
| Alchimie | Préparation de potions et activités associées |
| Ingénierie | Fabrication, utilisation et amélioration de machines |
| Technologies anciennes | Étude, découverte et réparation des installations de l'ancienne civilisation |

Cette liste est provisoire.

Nous pourrons ajouter, fusionner ou réorganiser certaines maîtrises après les premiers tests.

## 6.3. Aucune restriction des capacités vanilla

**Règle impérative : le système de maîtrise ne doit jamais retirer au joueur une capacité qu'il possède normalement dans Minecraft.**

Un joueur de niveau 1 en minage peut toujours miner du diamant avec une pioche adaptée.

Un joueur débutant peut construire, cultiver, pêcher et combattre normalement.

Les maîtrises doivent apporter des bonus, pas imposer des limitations artificielles.

## 6.4. Progression des niveaux

Chaque maîtrise dispose de son propre niveau et de sa propre expérience.

La progression doit être théoriquement infinie.

Les niveaux 1 à 100 représentent la progression principale.

Après le niveau 100, le joueur continue à progresser, mais avec des avantages plus modestes.

Nous avons envisagé :

- Un point de talent par niveau jusqu'au niveau 100.
- Un point de talent supplémentaire tous les dix niveaux ensuite.
- Des bonus de rendement décroissants après le niveau 100.
- Des récompenses de prestige périodiques.

Ces valeurs devront être ajustées en fonction de l'équilibrage réel.

## 6.5. Arbres de talents

Chaque maîtrise possède son propre arbre de compétences.

Le joueur utilise ses points de talents pour débloquer des améliorations.

Tous les talents doivent pouvoir être débloqués à terme.

Il ne faut pas créer de spécialisations mutuellement exclusives et définitives.

### Exemple : minage

Talents possibles :

- Augmentation progressive de la vitesse d'extraction.
- Probabilité d'obtenir davantage de ressources.
- Amélioration de l'efficacité de certains outils.
- Capacité à détecter approximativement des minerais proches.
- Capacité avancée à ressentir la présence d'un type de minerai recherché.

La détection des minerais doit être un véritable talent déblocable, et pas simplement une augmentation statistique.

### Exemple : ingénierie

Talents possibles :

- Amélioration du rendement des machines.
- Réduction de la consommation énergétique.
- Augmentation de la vitesse de production.
- Amélioration des capacités de stockage.
- Optimisation des systèmes d'extraction automatisée.

### Exemple : agriculture

Talents possibles :

- Augmentation du rendement des récoltes.
- Amélioration de la vitesse de certaines opérations agricoles.
- Réduction de la consommation de ressources des installations agricoles.

Ces exemples ne constituent pas des arbres définitifs.

## 6.6. Synergies entre maîtrises

Certaines maîtrises doivent pouvoir fonctionner ensemble.

Par exemple, un joueur expérimenté en minage et en ingénierie pourrait obtenir des améliorations spécifiques sur ses machines d'extraction.

Les synergies doivent encourager la progression dans plusieurs domaines.

Elles ne doivent pas obliger le joueur à adopter une classe particulière.

## 6.7. Progression après le niveau 100

Les récompenses postérieures au niveau 100 doivent conserver un intérêt sans créer une croissance incontrôlable.

Nous avons envisagé des bonus de rendement de plus en plus faibles, ainsi que des récompenses cosmétiques ou symboliques.

Exemples :

- Titres.
- Effets visuels.
- Animations.
- Décorations.
- Indicateurs de prestige.

L'objectif est qu'un joueur atteignant le niveau 500 ou 1000 conserve une satisfaction de progression, sans devenir infiniment plus puissant qu'un joueur de niveau 100.

## 6.8. Expérience et automatisation

Les machines automatiques ne doivent pas permettre de faire progresser artificiellement toutes les maîtrises sans intervention du joueur.

Par exemple, une machine qui extrait des minerais ne doit pas accorder automatiquement toute l'expérience de minage correspondante.

Il faut distinguer les actions manuelles et les actions automatisées.

L'ingénierie pourra progresser grâce à la conception, à l'amélioration et à l'utilisation pertinente des technologies.

Les règles précises d'attribution de l'expérience devront être définies avec des protections contre les boucles d'exploitation.

---

# 7. Automatisation et industrie

## 7.1. Objectif principal

L'automatisation constitue l'un des piliers majeurs du mod.

Le joueur doit pouvoir construire des installations qui récoltent et produisent des ressources pendant qu'il explore, combat ou effectue d'autres activités.

Exemple représentatif :

Le joueur construit une machine d'extraction de minerais, la configure, l'alimente, puis part explorer un donjon.

Lorsqu'il revient, la machine a récolté des ressources, potentiellement des diamants.

Ce principe doit pouvoir s'étendre à plusieurs types de production.

## 7.2. Liberté technologique

Nous ne sommes pas attachés à une seule forme d'automatisation.

Elle peut reposer sur :

- Des machines.
- Des mécanismes énergétiques.
- Des systèmes d'extraction.
- Des créatures apprivoisées.
- Des ouvriers ou entités autonomes.
- Des technologies anciennes.

Les premières versions devraient toutefois privilégier les machines, plus faciles à tester et à équilibrer.

## 7.3. Machines envisagées

À titre de pistes de développement :

- Extracteur minier automatique.
- Système de collecte de ressources.
- Machine de transformation de matériaux.
- Système de stockage.
- Transporteur de ressources.
- Générateur ou alimentation énergétique.
- Dispositif d'amélioration de rendement.
- Installation agricole automatisée.

Cette liste reste ouverte.

## 7.4. Progression industrielle

Les machines les plus puissantes doivent nécessiter des matériaux issus de la nouvelle dimension.

Il doit être possible de débuter avec des systèmes modestes, puis d'accéder à des technologies de plus en plus performantes.

Les machines avancées pourront notamment dépendre de matériaux, modules ou connaissances obtenus dans les ruines anciennes.

Le joueur doit progressivement passer de l'extraction manuelle à une production industrielle avancée.

## 7.5. Fonctionnement pendant l'absence du joueur

Il est important que les machines puissent fonctionner pendant que le joueur explore ailleurs.

Il faudra toutefois concevoir soigneusement le fonctionnement des machines dans les chunks non chargés.

L'objectif fonctionnel est que le joueur puisse revenir après une expédition et constater une production cohérente.

La solution technique reste à choisir : simulation de production hors chargement, chargement contrôlé de chunks ou autre mécanisme fiable.

**Il ne faut pas imposer le chargement permanent de grandes zones du monde si une simulation serveur plus légère peut obtenir le même résultat.**

Le système doit fonctionner correctement en multijoueur.

---

# 8. Créatures et familiers

## 8.1. Philosophie

La dimension doit contenir quelques créatures originales, mais celles-ci doivent rester rares.

Il ne faut pas perdre l'impression d'un monde déserté.

Certaines créatures pourront être apprivoisées et devenir des familiers.

D'autres pourront être des entités artificielles, des survivances biologiques ou des mécanismes anciens autonomes.

## 8.2. Utilité

Les familiers pourront potentiellement remplir différents rôles :

- Accompagnement.
- Assistance au combat.
- Collecte de ressources.
- Aide à l'exploration.
- Fonctions utilitaires particulières.

Ces rôles restent à concevoir.

## 8.3. Transport entre dimensions

Un familier apprivoisé doit pouvoir accompagner son propriétaire dans l'Overworld, le Nether, l'End et la nouvelle dimension, sous réserve des règles spécifiques à certaines créatures.

La gestion des changements de dimension, de la propriété et de la persistance devra être fiable.

## 8.4. Direction artistique

Les créatures doivent être originales.

Il ne faut pas simplement recolorer les animaux de Minecraft.

Elles peuvent présenter des caractéristiques biologiques inhabituelles, des comportements étranges ou des éléments technologiques.

Les espèces, modèles et animations seront définis ultérieurement.

---

# 9. Donjons et exploration

## 9.1. Objectif

La nouvelle dimension doit offrir des lieux dangereux et intéressants à explorer.

Les donjons et installations anciennes doivent récompenser la curiosité.

Ils pourront contenir des ressources rares, des composants technologiques, des équipements, des indices narratifs et des mécanismes à réparer.

## 9.2. Difficulté

Nous souhaitons une expérience globalement plus difficile que Minecraft classique.

Cependant, la difficulté doit provenir de dangers intéressants et de mécaniques à comprendre.

Il ne faut pas simplement multiplier les points de vie ou les dégâts des ennemis.

L'exploration doit être exigeante, sans devenir injuste.

## 9.3. Types de défis

Plusieurs possibilités pourront être étudiées :

- Combat contre des créatures ou systèmes défensifs.
- Énigmes environnementales.
- Réactivation de machines.
- Exploration conditionnée par le jour ou la nuit.
- Gestion de ressources énergétiques.
- Découverte de passages ou mécanismes cachés.

Les donjons et boss précis restent à imaginer.

---

# 10. Accès à la nouvelle dimension

Cette partie a fait l'objet de décisions détaillées et doit être considérée comme l'un des systèmes prioritaires du développement.

## 10.1. Structure ancienne proche du spawn

Lors de la génération d'un nouveau monde, une grande structure appartenant à l'ancienne civilisation doit apparaître relativement près du point d'apparition initial du joueur.

Une distance indicative de 500 à 1 500 blocs a été envisagée.

La structure doit être suffisamment visible ou découvrable pour qu'un joueur explorant normalement les environs ait de bonnes chances de la rencontrer.

Il ne faut pas qu'elle soit dissimulée à des dizaines de milliers de blocs.

Il s'agit du premier contact du joueur avec l'histoire du mod.

## 10.2. Nature de la structure

Nous l'avons provisoirement décrite comme un laboratoire ou un avant-poste scientifique.

Son architecture exacte reste à définir.

Elle doit être relativement grande et contenir plusieurs espaces explorables.

Elle doit immédiatement se distinguer des structures vanilla.

L'ambiance doit évoquer une installation très ancienne, technologiquement avancée et abandonnée.

## 10.3. L'objet mystérieux

Dans cette structure, le joueur découvre un objet ancien.

Nom provisoire : **Noyau de localisation**.

Cet objet constitue la base du futur détecteur permettant de trouver le portail.

Il est initialement inactif.

Le joueur doit comprendre qu'il lui manque une source d'énergie particulière.

## 10.4. Narration par fresques

Les explications doivent principalement être visuelles.

Nous ne souhaitons pas qu'un PNJ explique directement la marche à suivre ni qu'un journal de quête fournisse immédiatement toutes les réponses.

La structure doit contenir des gravures, symboles, schémas ou fresques.

Ces représentations doivent montrer :

1. Trois ressources distinctes.
2. Leur combinaison en un cristal unique.
3. L'insertion de ce cristal dans l'objet mystérieux.
4. L'utilisation du détecteur pour trouver une autre installation.

La compréhension doit être intuitive.

## 10.5. Les trois minerais

Trois minerais extrêmement rares doivent apparaître dans l'Overworld.

Ils sont liés à l'ancienne civilisation et à la nouvelle dimension.

Leur rareté doit être exceptionnellement élevée.

Leur présence dans l'Overworld pourra être expliquée par d'anciennes expérimentations de cette civilisation.

### Minerai bleu

Il doit être trouvé dans des environnements océaniques profonds.

Son emplacement précis et ses conditions de génération restent à déterminer.

La fresque correspondante doit montrer clairement un environnement sous-marin profond.

### Minerai rouge

Il doit être trouvé dans les grandes profondeurs de l'Overworld, au contact ou à proximité de la lave.

Nous avons notamment envisagé des formations sous la lave.

La fresque doit montrer le minerai associé à un environnement souterrain et volcanique.

### Minerai vert

Il doit apparaître dans des forêts extrêmement denses, près du niveau du sol.

Il faudra définir les biomes compatibles ou créer des conditions de génération particulières.

La fresque doit associer ce minerai à une végétation forestière dense.

## 10.6. Rareté et accessibilité

Ces minerais doivent être très difficiles à trouver, potentiellement plus rares que les ressources les plus précieuses de Minecraft vanilla.

Toutefois, leur recherche doit reposer sur l'exploration et l'observation, pas uniquement sur des dizaines d'heures de minage aléatoire.

Les fresques doivent donner de véritables indices.

Les taux de génération précis seront à équilibrer.

## 10.7. Fusion des minerais

Les trois minerais doivent pouvoir être combinés pour produire un cristal composite.

Le cristal doit avoir une apparence particulière, avec une identité visuelle reconnaissable.

Le procédé exact de fusion reste à définir.

Il peut s'agir d'une recette de fabrication spécifique ou d'un mécanisme ancien.

Pour la première version, une recette simple pourra servir de prototype.

## 10.8. Fabrication du détecteur

Le cristal composite est ensuite combiné au Noyau de localisation.

Le résultat est un détecteur fonctionnel.

Ce détecteur doit permettre de localiser un portail ancien dans l'Overworld.

## 10.9. Fonctionnement du détecteur

Nous avons envisagé une détection progressive.

À grande distance, l'objet pourrait indiquer une direction approximative.

En se rapprochant, il fournirait des informations plus précises.

À courte distance, il pourrait afficher les coordonnées exactes du portail.

Cette progression est une proposition à conserver pour les tests.

L'objet doit réellement localiser une structure générée dans le monde, et non des coordonnées fictives.

## 10.10. Génération des portails

Les portails doivent apparaître dans des structures anciennes extrêmement rares.

Nous avons évoqué un espacement de l'ordre de 10 000 à 15 000 blocs.

Il s'agit d'un ordre de grandeur, pas d'une règle algorithmique définitive.

La génération doit éviter les chevauchements et rester déterministe à partir de la seed.

## 10.11. Activation du portail

Une fois arrivé au portail, le joueur doit pouvoir séparer son détecteur en deux composants :

- Le Noyau de localisation.
- Le cristal composite.

Le cristal composite est inséré dans le mécanisme d'alimentation du portail.

Cette opération active définitivement le portail.

Le joueur conserve le Noyau de localisation.

Le détecteur est donc à la fois l'instrument qui permet de trouver le portail et le moyen d'obtenir sa source d'alimentation.

## 10.12. Persistance et indestructibilité

**Décisions définitives :**

Le portail reste ouvert une fois activé.

Il fonctionne dans les deux sens.

Le portail de l'Overworld conduit à la nouvelle dimension.

Un portail correspondant est présent à l'arrivée dans cette dimension.

Le portail de retour reste lui aussi ouvert.

Les portails doivent être indestructibles, sur le modèle des structures de portail de l'End.

Il ne doit pas être nécessaire de payer une ressource ou de refaire la quête à chaque traversée.

En multijoueur, une fois activé, le portail est accessible à tous les joueurs.

Les nouveaux joueurs n'ont pas besoin de recommencer la quête pour rejoindre leurs amis.

## 10.13. Points techniques particuliers

Le système doit gérer correctement :

- Les téléportations aller-retour.
- Les positions d'arrivée sûres.
- La persistance de l'activation.
- Les redémarrages du serveur.
- Les connexions et déconnexions.
- La traversée par plusieurs joueurs.
- Les familiers et entités compatibles.
- Les situations où le joueur meurt dans la nouvelle dimension.

Le portail doit rester accessible et utilisable même après un redémarrage ou un incident de serveur.

---

# 11. Histoire principale et progression narrative

## 11.1. Découverte

Le joueur découvre une structure ancienne dans l'Overworld.

Il trouve le Noyau de localisation et les fresques.

Il récolte les trois minerais, active son détecteur et découvre le portail.

Il entre dans une dimension qui semble morte.

## 11.2. Exploration de la civilisation

Au fil de l'exploration, le joueur découvre les vestiges d'une civilisation extrêmement avancée.

Il trouve des machines, des structures et des technologies incompréhensibles.

Il commence à comprendre que cette civilisation a exploité intensivement les ressources de son monde.

Son environnement est désormais délabré, déserté et partiellement détruit.

## 11.3. Révélation

Progressivement, le joueur découvre que les anciens habitants ne sont pas morts.

Ils ont volontairement accédé à une quatrième dimension spatiale.

Leur départ s'est déroulé progressivement.

Ils continuent potentiellement d'observer ou d'influencer le monde tridimensionnel.

Les anomalies rencontrées pendant l'exploration prennent alors un nouveau sens.

## 11.4. Le parallèle avec le joueur

En exploitant les ressources et technologies anciennes, le joueur reproduit progressivement certains comportements de la civilisation disparue.

Il construit des machines toujours plus puissantes.

Il automatise sa production.

Il exploite les ressources de son environnement.

Il atteint un niveau de puissance extraordinaire.

Cependant, il découvre également des indices suggérant que certaines technologies peuvent avoir des conséquences catastrophiques.

L'histoire doit lui permettre de comprendre, au moins partiellement, les risques qu'il prend.

Le scénario ne doit pas se limiter à une punition totalement imprévisible.

---

# 12. La machine mystérieuse

## 12.1. Présentation

Au cours de l'aventure, le joueur devra reconstruire une machine ancienne particulière.

Cette reconstruction sera nécessaire à la progression scénaristique.

Cependant, le joueur ne comprendra pas immédiatement son utilité.

La machine semblera inactive ou inutile pendant une grande partie de l'aventure.

## 12.2. Rôle véritable

Cette machine est au centre de la conclusion du mod.

Elle est capable de restaurer une configuration spatiale antérieure du monde.

Pour un observateur tridimensionnel, son fonctionnement donne l'impression d'un retour dans le temps.

En réalité, selon notre proposition scénaristique, elle exploite des propriétés de la quatrième dimension spatiale.

Cette explication est fictionnelle, mais doit rester cohérente avec les règles internes de notre univers.

## 12.3. Préparation narrative

Le joueur doit progressivement découvrir des indices sur cette machine sans connaître sa véritable fonction.

Son utilité doit être révélée au moment de la catastrophe finale.

---

# 13. Conclusion scénaristique : la catastrophe

## 13.1. Principe

À un stade avancé de l'histoire, les technologies issues de l'ancienne dimension provoquent une catastrophe dans l'Overworld.

Les machines et installations liées à cette technologie entrent dans une réaction en chaîne.

Elles explosent et provoquent la destruction d'une partie importante des constructions du joueur.

L'objectif est de lui faire vivre un choc émotionnel.

Après avoir passé des dizaines, voire des centaines d'heures à construire sa base et ses infrastructures, il voit son monde ravagé par les technologies qu'il a lui-même adoptées.

## 13.2. Signification narrative

La catastrophe doit refléter les conséquences de l'exploitation excessive des technologies anciennes.

Le joueur découvre qu'il a reproduit les erreurs de la civilisation qu'il explorait.

Il avait potentiellement accès à des avertissements, mais a continué à développer ses installations.

Cette séquence constitue un point culminant de l'histoire.

## 13.3. Cinématique initiale

La catastrophe commence par une séquence cinématique.

Le joueur doit pouvoir observer les explosions depuis un point de vue extérieur.

Il voit les installations technologiques se détruire et les dégâts se propager.

La mise en scène doit être impressionnante, sans dépendre uniquement de dizaines de milliers d'explosions physiques coûteuses.

## 13.4. Exploration du monde détruit

Après la cinématique, le joueur reprend le contrôle.

Il doit être libre d'explorer une version détruite de son propre monde.

Il peut retourner sur les lieux de ses constructions.

Il peut constater l'ampleur des dégâts.

Il doit croire, pendant un certain temps, que la destruction est réelle et durable.

Il ne faut pas restaurer immédiatement le monde à la fin de la cinématique.

## 13.5. Ambiance de la catastrophe

La phase d'exploration doit être marquée par un silence inhabituel.

Les machines ne fonctionnent plus.

Les bâtiments sont détruits.

L'environnement semble instable.

Des bruits lointains et des anomalies visuelles peuvent ponctuer l'exploration.

## 13.6. Anomalies graphiques

Nous souhaitons notamment reproduire un effet de rémanence visuelle.

Cet effet ressemble à un affichage qui ne se rafraîchit pas correctement.

Lorsqu'un élément se déplace, certaines traces des images précédentes restent brièvement visibles.

Le joueur doit avoir l'impression que la réalité elle-même fonctionne mal.

D'autres effets peuvent être utilisés :

- Pixels corrompus.
- Artefacts visuels.
- Décalages temporaires.
- Distorsions discrètes.
- Perturbations de l'image.

Ces effets doivent être simulés intentionnellement et ne doivent pas compromettre la stabilité du jeu.

Ils devront être configurables, avec une option de désactivation pour l'accessibilité.

## 13.7. Déclenchement de l'indication

Pendant les premières minutes, le joueur ne reçoit pas immédiatement d'objectif.

Il explore librement.

Après environ deux à trois minutes, un événement se déclenche.

Nous avons envisagé :

1. Un son grave et inhabituel.
2. Une intensification temporaire des anomalies.
3. Une indication signalant qu'une structure demeure active.
4. Un guidage discret vers la machine mystérieuse.

Le texte exact et la présentation restent à définir.

## 13.8. Retour vers la machine

Le joueur doit pouvoir retrouver la machine qu'il avait reconstruite.

Plus il s'en approche, plus les anomalies visuelles peuvent diminuer.

La machine est devenue active.

Elle présente une interface étrange, évoquant une technologie supérieure.

Après une phase de traduction ou d'interprétation, elle propose une restauration.

Texte provisoire :

**« Configuration spatiale antérieure identifiée. Restauration possible. »**

## 13.9. Choix du joueur

Le joueur dispose de deux possibilités :

**Accepter :** restaurer le monde dans son état antérieur à la catastrophe.

**Refuser :** continuer à explorer le monde détruit.

Le refus ne doit jamais être définitif.

Le joueur doit pouvoir revenir à la machine plus tard et accepter la restauration.

Il ne doit pas y avoir de compte à rebours imposant une décision.

## 13.10. Restauration

Lorsque le joueur accepte, une séquence de restauration se déclenche.

Les structures peuvent visuellement se reconstituer.

Les machines réapparaissent.

Le monde retrouve son état antérieur.

Le joueur comprend alors la véritable utilité de la machine.

Cette séquence peut servir à révéler plus explicitement la nature de la technologie quadridimensionnelle.

---

# 14. Sécurité absolue de la catastrophe

Cette partie constitue une exigence technique majeure.

## 14.1. Règle fondamentale

**La séquence scénaristique ne doit jamais entraîner une perte irréversible des constructions, inventaires ou données du joueur.**

Une simple sauvegarde réalisée juste avant les explosions n'est pas considérée comme une protection suffisante.

Il faut concevoir le système en considérant les risques de crash, d'arrêt du serveur, de corruption de données et d'interruption de la restauration.

## 14.2. Architecture recommandée

Nous souhaitons séparer totalement l'état réel du monde et l'expérience de destruction.

L'approche privilégiée est la suivante :

1. Préserver un état restaurable et vérifié du monde.
2. Empêcher la catastrophe de modifier cet état de référence.
3. Créer un environnement temporaire ou une représentation isolée des destructions.
4. Permettre au joueur d'explorer cet environnement.
5. Restaurer le monde depuis l'état de référence lorsqu'il le décide.
6. Ne supprimer aucune donnée de récupération tant que la restauration n'est pas entièrement validée.

## 14.3. Isolation des données

Le monde d'origine doit rester intact pendant la séquence.

Les destructions doivent se produire dans une copie, un environnement isolé ou une simulation suffisamment fidèle.

Le choix technique doit être étudié avant toute implémentation.

En particulier, il ne faut pas déclencher de véritables explosions destructrices sur les seules données persistantes du monde original.

## 14.4. Gestion des inventaires

Les actions réalisées dans le monde détruit ne doivent pas provoquer de duplication d'objets.

Nous privilégions une restauration de l'état antérieur des inventaires et des ressources.

Les modifications temporaires effectuées pendant l'exploration des ruines ne sont pas conservées après restauration.

## 14.5. Résilience aux incidents

Le système doit prévoir :

- Un arrêt brutal pendant la cinématique.
- Un crash pendant l'exploration.
- Une déconnexion du joueur.
- Un redémarrage du serveur.
- Un arrêt pendant la restauration.
- Une restauration interrompue.
- Un manque d'espace disque.
- Une sauvegarde temporaire invalide.
- Une panne de chargement des chunks.
- Un serveur multijoueur avec plusieurs joueurs connectés.

Il faudra prévoir une procédure de récupération indépendante de l'interface scénaristique.

## 14.6. Exigence de validation

Aucune catastrophe réelle ne devra être activée sur une sauvegarde importante tant que la sécurité du système n'aura pas été démontrée par des tests.

Il est impossible de garantir mathématiquement l'absence de toute défaillance matérielle ou logicielle. L'objectif technique doit donc être une isolation stricte des données, des sauvegardes indépendantes et des procédures de récupération vérifiées.

---

# 15. Accès éventuel à la quatrième dimension spatiale

Nous avons évoqué une évolution de fin de jeu dans laquelle le joueur pourrait accéder à une forme de perception ou d'existence quadridimensionnelle.

Cette étape pourrait lui conférer des capacités extraordinaires.

Exemples envisagés :

- Percevoir des structures normalement invisibles.
- Interagir avec des mécanismes impossibles à comprendre auparavant.
- Modifier localement certaines propriétés de l'environnement.
- Manipuler des structures ou phénomènes spatiaux.

L'idée initiale est de donner au joueur une sensation de puissance presque absolue.

Cependant, le fonctionnement exact de cette phase n'est pas défini.

**Cette fonctionnalité ne doit pas être développée dans le premier prototype.**

Il faut conserver cette possibilité pour une phase ultérieure du projet.

---

# 16. Interfaces utilisateur

## 16.1. Interface des maîtrises

Le joueur doit pouvoir consulter :

- Ses quinze maîtrises.
- Leur niveau.
- Leur expérience actuelle.
- L'expérience nécessaire au prochain niveau.
- Les points de talents disponibles.
- Les arbres de talents.
- Les bonus actifs.

L'interface doit être claire, agréable et cohérente avec Minecraft.

## 16.2. Interface des machines

Les machines doivent disposer d'interfaces permettant de comprendre leur fonctionnement.

Selon le type de machine :

- Inventaire d'entrée.
- Inventaire de sortie.
- Énergie disponible.
- Progression du traitement.
- Production.
- Améliorations installées.
- État de fonctionnement.

## 16.3. Interface du détecteur

Le détecteur doit afficher les informations de localisation d'une manière cohérente avec son caractère ancien et mystérieux.

Il ne doit pas ressembler à un simple GPS moderne sans identité visuelle.

## 16.4. Interface de la machine de restauration

Cette interface doit être particulièrement travaillée.

Elle doit donner l'impression que le joueur interagit avec un dispositif dépassant sa compréhension.

Les choix doivent toutefois rester parfaitement lisibles et compréhensibles.

---

# 17. Modèles 3D, textures et animations

Le projet nécessite des ressources visuelles originales.

Cela inclut potentiellement :

- Blocs.
- Minerais.
- Machines.
- Structures.
- Objets.
- Interfaces.
- Créatures.
- Familiers.
- Animations.
- Particules.
- Effets visuels.

L'objectif est de conserver une cohérence avec l'univers cubique de Minecraft, tout en développant une identité science-fiction particulière.

Les modèles doivent être créés dans des formats compatibles avec le moteur et le système de rendu retenus.

Claude pourra proposer des modèles simples, fichiers de définition, textures procédurales ou outils de génération lorsque cela est techniquement adapté.

Pour les créations artistiques plus complexes, notamment les créatures animées, il faudra probablement utiliser des outils de modélisation comme Blockbench et un processus de validation visuelle.

Il ne faut pas prétendre qu'un simple fichier de code constitue automatiquement un modèle 3D artistique complet.

---

# 18. Multijoueur

Le mod doit être conçu dès le départ pour fonctionner sur serveur dédié.

Cela concerne :

- La progression individuelle des maîtrises.
- Les points de talents.
- Les machines partagées.
- La propriété des familiers.
- Les portails.
- La génération des structures.
- La synchronisation des événements.
- La persistance des données.
- Les interactions simultanées.

Le serveur doit rester l'autorité pour les systèmes de progression, les ressources et les machines.

La conclusion scénaristique nécessite une réflexion particulière.

Il faudra déterminer comment gérer une catastrophe globale lorsque plusieurs joueurs sont connectés et n'ont pas tous atteint le même stade de progression.

Cette question n'a pas encore été tranchée.

---

# 19. Architecture technique attendue

## 19.1. Choix technologiques

Nous avons envisagé Minecraft Java Edition 1.20.1 avec Forge.

Cependant, cette décision n'est pas définitive.

Avant de commencer, Claude doit comparer les options pertinentes, notamment la stabilité de l'écosystème, la documentation, les possibilités de génération de dimensions et les outils de développement.

Il doit recommander une version de Minecraft et un mod loader adaptés à un projet de longue durée.

Le choix final doit être validé avant la création du projet.

## 19.2. Modularité

Le code doit être organisé en systèmes indépendants.

Modules logiques envisagés :

- Core : initialisation et infrastructure commune.
- World : dimension et génération.
- Structures : bâtiments et portails.
- Resources : minerais et matériaux.
- Progression : niveaux et talents.
- Technology : machines et énergie.
- Entities : créatures et familiers.
- Narrative : histoire et événements.
- Anomalies : effets environnementaux.
- Client : interfaces, rendu et effets visuels.
- Networking : synchronisation multijoueur.
- Persistence : sauvegarde des données.

Il s'agit d'une séparation conceptuelle, pas nécessairement des noms exacts de packages Java.

## 19.3. Qualité du code

Nous attendons :

- Un code lisible et maintenable.
- Une séparation claire entre logique serveur et client.
- Une gestion robuste des données persistantes.
- Des identifiants cohérents.
- Des configurations externalisées lorsque c'est pertinent.
- Des systèmes extensibles.
- Une documentation minimale.
- Des tests pour les fonctionnalités critiques.

Il faut éviter les solutions temporaires qui rendraient difficile l'ajout de nouveaux contenus.

---

# 20. Première version jouable : MVP

Le projet complet représente un volume de travail considérable.

Nous ne souhaitons pas demander à Claude de programmer l'intégralité du mod en une seule génération.

Il faut procéder par étapes.

## Phase 0 — Fondations techniques

Objectif : obtenir un projet Minecraft moddé propre, compilable et exécutable.

Livrables :

- Projet Java initialisé.
- Configuration du mod loader.
- Enregistrement du mod.
- Structure de packages.
- Système de ressources.
- Configuration de base.
- Lancement client fonctionnel.
- Lancement serveur dédié fonctionnel.
- Documentation de compilation.

Critère de validation : le mod se lance sans erreur dans un environnement de développement et sur serveur dédié.

## Phase 1 — Première dimension

Objectif : créer un prototype de la nouvelle dimension.

Livrables :

- Dimension enregistrée.
- Génération de terrain initiale.
- Quelques blocs originaux.
- Un premier biome de test.
- Cycle jour-nuit spécifique.
- Ambiance visuelle et sonore provisoire.
- Moyen temporaire de rejoindre la dimension pour les tests.

À ce stade, il n'est pas nécessaire de créer les architectures définitives.

Le prototype doit démontrer que la dimension fonctionne correctement.

## Phase 2 — Première quête d'accès

Objectif : implémenter le cheminement menant au portail.

Livrables :

- Structure ancienne proche du spawn.
- Noyau de localisation.
- Trois minerais originaux.
- Conditions de génération spécifiques.
- Cristal composite.
- Détecteur.
- Localisation d'un portail.
- Structure de portail.
- Activation permanente.
- Traversée bidirectionnelle.
- Indestructibilité du portail.

Les fresques peuvent initialement utiliser des textures provisoires.

Leur version artistique définitive sera créée ultérieurement.

## Phase 3 — Maîtrises et talents

Objectif : implémenter une architecture de progression générique.

Livrables :

- Système d'expérience par maîtrise.
- Niveaux indépendants.
- Progression infinie.
- Points de talents.
- Sauvegarde individuelle.
- Interface de consultation.
- Premier arbre de talents.

Commencer par trois maîtrises réellement fonctionnelles :

- Minage.
- Bûcheronnage.
- Ingénierie.

L'architecture doit permettre d'ajouter les douze autres sans réécrire le système.

## Phase 4 — Première machine automatisée

Objectif : créer une machine capable de produire des ressources automatiquement.

Livrables :

- Bloc de machine.
- Interface.
- Inventaire interne.
- Consommation énergétique ou de ressources.
- Production automatique.
- Persistance de l'état.
- Fonctionnement multijoueur.
- Premier système d'amélioration.

Pour le prototype, nous privilégions une machine minière.

Elle doit pouvoir produire des ressources selon des règles configurables, puis permettre au joueur de récupérer sa production après une période d'exploration.

## Phase 5 — Première boucle de gameplay

Objectif : relier les systèmes précédents.

Exemple de boucle :

Le joueur découvre la structure initiale, trouve les minerais, active le portail, explore la dimension, récupère une ressource technologique, fabrique une machine dans l'Overworld, puis utilise sa production pour progresser.

Cette première boucle doit être réellement jouable.

Elle doit démontrer l'intérêt de la combinaison exploration, RPG et automatisation.

## Phase 6 — Extension progressive

Après validation du MVP :

- Nouveaux biomes.
- Structures et donjons.
- Autres maîtrises.
- Arbres de talents complets.
- Machines supplémentaires.
- Premières créatures.
- Familiers.
- Événements horrifiques.
- Systèmes diurnes et nocturnes avancés.
- Développement du scénario.
- Technologies quadridimensionnelles.
- Conclusion scénaristique.

La catastrophe finale doit être développée très tardivement, lorsque les systèmes de sauvegarde et de récupération auront été solidement testés.

---

# 21. Instructions à Claude pour commencer

Tu es chargé de participer au développement d'un mod Minecraft Java original.

Le présent document constitue la référence fonctionnelle et créative du projet.

Ta première mission n'est pas de développer immédiatement toutes les fonctionnalités décrites.

Tu dois commencer par analyser le cahier des charges et proposer une architecture technique réaliste.

Nous attendons de toi :

1. Une recommandation argumentée concernant la version de Minecraft, le mod loader et les dépendances nécessaires.
2. Une architecture de code extensible adaptée aux systèmes décrits.
3. Un découpage des premières étapes de développement.
4. L'identification des principales difficultés techniques.
5. La définition des éléments nécessaires au premier prototype jouable.
6. La génération d'un projet initial compilable et testable, après validation des choix structurants.

Tu dois distinguer les décisions définitivement prises des idées encore ouvertes.

Tu ne dois pas transformer une suggestion créative en règle définitive sans validation.

Lorsque plusieurs solutions techniques existent, privilégie celle qui garantit la maintenabilité, la stabilité et la compatibilité multijoueur.

Ne présente jamais du pseudocode comme une implémentation fonctionnelle.

Ne prétends pas qu'un système a été testé si aucun test n'a réellement été exécuté.

Si tu ne peux pas vérifier une API ou une dépendance, indique clairement ce qui reste à confirmer.

Privilégie des fonctionnalités réellement compilables et testables à une grande quantité de code inachevé.

Pour la première itération, concentre-toi sur les fondations techniques et sur la création d'une dimension de test.

**Objectif immédiat : obtenir une première version du mod qui démarre correctement, possède une nouvelle dimension accessible en développement et fournit une base saine pour construire les systèmes suivants.**

---

# 22. Décisions définitives et questions ouvertes

## Décisions déjà prises

- Développer un mod original plutôt qu'un assemblage de mods existants.
- Préserver l'expérience vanilla de Minecraft.
- Concentrer la majorité des nouveautés dans une dimension originale.
- Autoriser l'utilisation des machines et ressources dans l'Overworld.
- Créer une civilisation ancienne passée volontairement et progressivement à une quatrième dimension spatiale.
- Donner à la nouvelle dimension une atmosphère morte et oppressante.
- Intégrer une horreur principalement psychologique.
- Conserver un cycle jour-nuit ayant une influence réelle sur le gameplay.
- Prévoir des créatures rares et des familiers.
- Développer environ quinze maîtrises.
- Autoriser une progression infinie.
- Permettre de débloquer tous les talents à terme.
- Ne jamais restreindre les capacités normales du joueur débutant.
- Donner une place centrale à l'automatisation.
- Introduire une structure ancienne relativement proche du spawn.
- Utiliser trois minerais spécifiques pour activer un détecteur.
- Placer ces minerais dans trois environnements distincts.
- Utiliser le détecteur pour trouver le portail.
- Consommer le cristal composite pour activer le portail.
- Rendre les portails permanents, bidirectionnels et indestructibles.
- Permettre aux joueurs d'un serveur de partager un portail activé.
- Prévoir une catastrophe scénaristique détruisant apparemment les constructions.
- Permettre au joueur d'explorer librement les ruines.
- Introduire des anomalies graphiques pendant cette séquence.
- Donner au joueur le choix du moment de restauration.
- Garantir une architecture de récupération qui préserve les données originales.

## Paramètres provisoires

- Minecraft 1.20.1 et Forge.
- Cycle de 40 minutes.
- Structure initiale située à environ 500–1 500 blocs du spawn.
- Portails espacés d'environ 10 000–15 000 blocs.
- Niveau 100 comme fin de la progression principale des talents.
- Un point de talent par niveau jusqu'au niveau 100.
- Un point supplémentaire tous les dix niveaux ensuite.
- Déclenchement de l'indication de restauration après deux à trois minutes.
- Liste exacte des quinze maîtrises.
- Nom et apparence des ressources, structures et objets.

## Questions encore ouvertes

- Nom du mod et de la dimension.
- Choix final de la version Minecraft et du mod loader.
- Biomes exacts de la nouvelle dimension.
- Architecture définitive des structures.
- Liste et propriétés des matériaux.
- Fonctionnement énergétique des machines.
- Équilibrage de la production automatique.
- Contenu exact des arbres de talents.
- Nombre et comportement des créatures.
- Système d'apprivoisement.
- Types de donjons et de boss.
- Progression précise de l'histoire.
- Rôle final des êtres quadridimensionnels.
- Fonctionnement de l'accès éventuel à la quatrième dimension.
- Gestion de la catastrophe en multijoueur.
- Technologie de simulation et de restauration sécurisée du monde.

---

# 23. Résultat recherché

À terme, le joueur doit pouvoir commencer une partie de Minecraft presque normale, découvrir progressivement les traces d'une civilisation inconnue, pénétrer dans une dimension mystérieuse, développer ses maîtrises, explorer des ruines, découvrir des technologies extraordinaires et construire des installations de plus en plus puissantes.

Il doit éprouver la satisfaction de passer d'un simple aventurier à un individu capable d'automatiser une grande partie de sa production.

Mais derrière cette progression doit subsister un mystère : qui a construit ces machines, pourquoi ce monde est-il abandonné, et que sont devenus ses habitants ?

L'histoire doit progressivement révéler que cette civilisation n'a pas disparu, mais qu'elle a dépassé les limites de l'espace tridimensionnel.

Enfin, le joueur devra faire face aux conséquences de son propre développement technologique, avant de découvrir qu'une machine reconstruite au cours de son aventure possède le pouvoir de restaurer son monde.

**Notre objectif est de créer un mod qui donne envie d'explorer, de comprendre, de construire, de progresser et de revenir jouer pendant des centaines d'heures, tout en proposant une identité artistique et narrative réellement originale.**

*Fin du cahier des charges — version 0.1.*