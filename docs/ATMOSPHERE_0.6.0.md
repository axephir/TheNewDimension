# Ambiance sonore et paysages — 0.6.0-dev

Minecraft Java 26.2 / NeoForge 26.2.0.93 / Java 25. La 0.6 conserve les sanctuaires, familiers, ressources nocturnes, maîtrises et machines existants.

## Un paysage sonore plus présent dans Vestige

Dix-huit sons originaux sont fournis en Ogg Vorbis : six nappes propres aux biomes, deux couches de contexte, neuf événements ponctuels et un carillon de maîtrise. Les nappes durent 32 secondes, avec raccord en fondu et correction de la jonction. Il ne s'agit pas de sons récupérés d'un autre jeu ni d'enregistrements de voix.

Chaque biome possède un timbre distinct : vent chargé de basses dans les étendues, grondements de faille, résonances creuses, frémissements vitreux, souffles aquatiques ou frottements de cendres. La nuit ajoute une pression grave ; sous un toit ou sous terre, une nappe résonnante supplémentaire monte progressivement. Ces couches se fondent au changement de situation.

Des événements spatialisés se produisent à huit à dix-huit blocs du joueur, toutes les 35 à 100 secondes environ après une première attente. Leur palette dépend du biome et du couvert : tintements, raclements, déformations de souffle, pulsations et résonances sous les eaux. Un signal plus profond se produit toutes les trois à sept minutes environ. Au plus deux sons ponctuels sont suivis simultanément. Les attaques sont progressives, sans pic brutal de volume.

Le contrôleur ne fait que jouer du son local : aucun monstre caché, aucune recherche de structure, aucun chargement de chunk, aucun changement de bloc et aucun paquet réseau supplémentaire. Les compteurs s'arrêtent pendant une pause solo. Les couches supplémentaires sont supprimées en quittant Vestige ou la partie ; les boucles de biome utilisent les transitions natives de Minecraft.

Toutes les ambiances suivent le curseur Minecraft **Ambiance/Environnement**. Dans `config/tnd-client.toml`, `atmosphere.additionalLayers` active les couches nocturnes, intérieures et les événements locaux ; `additionalVolume` règle leur gain. Les six boucles de biome restent contrôlées par le curseur natif. Tous les sons ont des sous-titres français et anglais.

Un extrait audio distribué séparément assemble les fichiers réellement inclus dans le mod. C'est un montage d'écoute, pas un enregistrement du mixage en jeu ; le jeu ajoute l'atténuation, la position et les réglages de volume.

## Montées de maîtrise

Lorsqu'un gain d'XP franchit réellement un niveau, le serveur envoie au joueur un carillon original de 2,5 secondes, une spirale de particules lumineuses et un halo d'enchantement. Le message existant conserve le nom de la maîtrise et le nouveau niveau. Les quinze maîtrises et toutes les dimensions sont prises en charge.

Un gain qui ne change pas le niveau reste silencieux. Un gain qui franchit plusieurs niveaux produit une seule célébration pour le niveau atteint. La synchronisation du profil, l'ouverture du menu et la connexion ne rejouent pas ces effets. Les sons et particules ciblent leur propriétaire, pour éviter d'envahir les autres joueurs. Le son suit le curseur **Joueurs** et les particules respectent les options natives de rendu.

## Trois nouveaux biomes, six au total

| Biome | Terrain et éléments visibles | Ambiance |
|---|---|---|
| Forêt de verre | Sol de pierre vitreuse, reliefs modérés, formations cristallines ramifiées de 7 à 12 blocs | Frémissements, tintements, lueurs pâles et brume froide |
| Bassins de cendre | Terrain plus bas, limon, petites mares bordées, souches et colonnes fossilisées | Eaux sombres, souffles graves et brume rapprochée |
| Dunes vitrifiées | Relief ondulé, cendre vitrifiée, crêtes minérales sombres | Frottements de poussière, grondements et cendres en suspension |

Les étendues silencieuses, champs fracturés et corniches creuses reçoivent aussi leur propre nappe sonore et des vestiges minéraux plus espacés. La sélection des six biomes utilise deux bruits climatiques indépendants. Les trois nouveaux terrains possèdent leurs propres fonctions de densité et leurs couches de surface. Les densités se mélangent progressivement aux frontières climatiques. Ils ne se distinguent pas uniquement par une couleur de brouillard.

Les pas et la récolte utilisent aussi des timbres adaptés au limon, aux cendres, à la pierre vitreuse et aux fossiles. Les cinq matériaux supplémentaires sont récupérables en survie et possèdent des textures originales 16 × 16. Les mares ont un fond et des bordures pleines. Les éléments de paysage préparent toute leur zone avant de poser des blocs, restent dans leur chunk et refusent les volumes contenant une construction. Les structures de Vestige peuvent apparaître dans les six biomes. Les familiers, sentinelles et agrégats reconnaissent les nouveaux sols naturels.

Aucune population dense n'est ajoutée : la dimension conserve son caractère abandonné. Les volumes de cristal et les vestiges donnent des repères sans multiplier les monstres.

## Mise à jour et validation

Remplacer l'ancien JAR TND par `tnd-0.6.0-dev.jar`. L'audio et les montées de niveau fonctionnent dans les parties existantes. Les biomes et formations supplémentaires demandent **de nouveaux chunks de Vestige** ; les constructions et chunks déjà enregistrés ne sont pas réécrits.

- Build Java 25 et 50 114 assertions du domaine réussis.
- 33 GameTests requis : les six biomes sont trouvés par le véritable échantillonneur climatique ; les formations des trois nouveaux biomes sont générées réellement ; les mares restent fermées et un bloc de construction est conservé.
- Les quinze maîtrises sont testées avec de vrais paquets de son et de particules, sans célébration sous le seuil, sans diffusion aux autres joueurs et sans rejeu lors d'une synchronisation.
- 13 contrôles de ressources, dont 66 textures toutes en 16 × 16.
- Les 18 fichiers Ogg distribués sont décodés et mesurés avec ffmpeg : durée, signal audible, marge avant saturation et raccord des boucles.

Les boss, l'industrie avancée et le scénario final restent à développer. Aucun événement destructeur n'est ajouté.
