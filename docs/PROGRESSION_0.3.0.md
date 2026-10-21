# Progression et ruines interactives — 0.3.0-dev

Minecraft Java 26.2, NeoForge 26.2.0.93, Java 25. Cette version conserve la refonte graphique et architecturale de la 0.2.

## Quinze maîtrises jouables

Chaque maîtrise possède maintenant des gains d’XP en survie, cinq talents achetables, une perception active et une activité indiquée dans l’interface. Les capacités vanilla restent accessibles dès le départ. Les XP et les talents existants sont conservés ; la progression persiste après la mort.

| Maîtrise | Actions récompensées | Efficacité par rang |
|---|---|---|
| Minage | Minerais naturels récoltés manuellement | Vitesse de minage +5 % |
| Bûcheronnage | Troncs récoltés manuellement | Vitesse de coupe +5 % |
| Agriculture | Récolte de cultures mûres, cacao et verrues mûrs | Toutes les 5 s, chance de 4 % de faire pousser une culture proche |
| Pêche | Prises réussies à la canne | Chance +0,2 avec une canne en main |
| Chasse | Animaux tués par le joueur | Dégâts aux animaux +2 % |
| Élevage | Naissances attribuées au joueur | Vie maximale des petits +4 %, conservée sur l’animal |
| Combat rapproché | Monstres vaincus au contact | Dégâts aux créatures +3 % |
| Combat à distance | Monstres vaincus par projectile | Dégâts aux créatures +3 % |
| Défense | Attaques de créatures encaissées ou bloquées au bouclier | Dégâts reçus des créatures −2 % |
| Artisanat | Objets fabriqués, avec budget par type | XP de toutes les maîtrises +5 % |
| Construction | Première construction dans une cellule de 4 × 4 × 4 blocs récemment non utilisée | Portée +0,15 bloc avec un bloc en main |
| Exploration | Découverte de territoires de 64 × 64 blocs | Vitesse de déplacement +1,5 % |
| Alchimie | Potions récupérées dans l’alambic ou bues | Durée des potions bénéfiques bues +5 % |
| Ingénierie | Fabrication et calibration des machines | Cadence après réglage +5 % |
| Technologies anciennes | Minerai résonant, premières lectures des quatre fresques, consoles étudiées | Calibration de relique +2 points, base 15 %, plafond 25 % |

Rendement offre 2 % de chance d’un objet supplémentaire par rang pour les cinq maîtrises de récolte, au maximum un objet par action. En ingénierie il augmente la cadence de 3 % par rang ; dans les autres maîtrises il augmente l’XP de cette maîtrise de 2 % par rang. Les bonus de dégâts concernent les créatures, sans augmenter le PvP.

Perception se lance depuis le bouton « Percevoir » de l’interface ou `/tnd sense <maîtrise>`. Elle cherche une cible adaptée — animaux, monstres, cultures mûres, minerais, ateliers, coffres, machines ou vestiges — uniquement dans les chunks déjà chargés. Une indication de distance et de direction et un chemin de particules sont envoyés au joueur. La recharge de cinq secondes est commune à toutes les maîtrises. Précision étend le rayon de 6 à 16 blocs ; elle augmente aussi la cadence en ingénierie. Résonance ajoute 2 % d’XP par rang partagé avec la maîtrise indiquée dans l’infobulle ; les synergies de récolte et d’ingénierie ajoutent aussi leur bonus de vitesse ou de cadence.

Les gains apparaissent en survie. Aucun XP n’est attribué à la production automatique, au mode créatif ou à la croissance passive des cultures. Les achats de talents restent vérifiés par le serveur : points, prérequis, rang maximal et identifiant de bouton.

## Consoles, fresques et reliques

Cliquer sur une fresque révèle un indice sur les minerais et le fonctionnement des vestiges. La première lecture de chaque motif naturel donne de l’XP de technologie ancienne.

Une console éteinte se répare avec un éclat résonant, tenu en main principale ou secondaire. Elle s’allume, émet un son et des particules, et une console naturelle donne **une seule fois au total** un ou deux fragments de relique. Les consoles posées par les joueurs n’en donnent pas. L’état réparé est un état de bloc sauvegardé, partagé par tous les joueurs.

Une console active calibre les extracteurs dans une zone de 13 × 9 × 13 blocs, sans charger les chunks voisins. Le bonus de relique persiste sur la machine et ne diminue pas si un joueur moins spécialisé l’utilise. Les avant-postes, relais et fonderies existants contiennent déjà des consoles utilisables. Les nouveaux modèles d’archive et de sanctuaire en contiennent également ; ces ajouts de mobilier apparaissent dans les nouveaux chunks.

Trois fragments, un cristal composite, un diamant et un lingot de cuivre permettent de fabriquer le module de stabilisation. Découvrir un éclat résonant débloque les recettes du mod dans le livre de recettes vanilla. Des infobulles expliquent les modules et les fragments.

## Extraction

| Profil | Produit | Durée de base relative | Condition |
|---|---|---|---|
| Fer | Fer brut | ×1 | Charbon ou charbon de bois |
| Cuivre | Cuivre brut | ×1 | Idem |
| Or | Or brut | ×3 | Idem |
| Redstone | Poudre de redstone | ×2 | Idem |
| Diamant | Diamant | ×8 | Module de stabilisation obligatoire |

Par défaut le cycle de base dure 60 secondes. Chaque objet consomme un charbon. Le module d’efficacité ajoute 50 % de cadence ; le module de stabilisation ajoute 25 % et autorise le diamant. Un seul module occupe le logement physique. Les talents et le bonus de relique s’ajoutent à la cadence ; le temps de cycle est divisé par cette cadence.

Les cinq icônes de l’interface sélectionnent le profil. La sortie doit être vide avant de changer et le travail partiel est alors remis à zéro. Aucun objet déjà produit ne peut être converti en une autre ressource. Retirer le stabilisateur suspend le diamant, y compris lors du rattrapage après rechargement. Le temps retenu est celui du monde : aucune production pendant l’arrêt du serveur. Le rattrapage reste plafonné à six heures par défaut, avec inventaire et carburant finis.

Les machines anciennes rechargent leur profil fer par défaut ; inventaire, progression et réglages sont conservés.

## Protections et limites

Un historique sauvegardé côté serveur limite les fabrications répétées, les réglages de machines, l’étude des consoles et l’XP défensive. La construction et l’exploration mémorisent les territoires récents. L’historique est borné à 2 048 entrées par joueur : les plus anciennes peuvent être évincées lors d’expéditions prolongées. Les ressources placées par les joueurs restent marquées dans leur chunk pour éviter le cycle simple pose/récolte. Les déplacements de blocs par pistons et les interactions avec d’autres mods ne sont pas encore garantis contre tous les détournements.

Cette version fournit une première progression pour les quinze maîtrises ; elle ne réalise pas encore l’ensemble du cahier des charges. Créatures originales, boss, donjons de combat complets, réseau d’énergie et autres machines industrielles, événements narratifs majeurs et scénario final restent à développer.

## Validation

- Build Java 25 et noyau : **50 114 assertions réussies**, dont les limites et la sauvegarde de l’historique, les quinze synergies et les invariants de production.
- Ressources : **10 tests réussis**, dont les références, 45 textures originales en 16 × 16, les cinq structures accessibles et couvertes, les 75 descriptions de talents en français et en anglais, les recettes et les deux états visuels des consoles.
- Minecraft : **16 GameTests requis réussis**, dont 15 du mod et un test natif. Ils couvrent les cinq profils d’extraction et leur carburant, les sauvegardes, le verrou du diamant, les récompenses des consoles, l’absence de récompenses créatives, les événements d’XP des quinze maîtrises, les achats et prérequis de tous les arbres, la perception et sa recharge commune, ainsi que les bonus de mêlée, défense, élevage, portée, déplacement, chance de pêche et durée des potions. Les régressions de portails, minerais, terrain et bâtiments passent également.
- Client normal : validation graphique et fonctionnelle décrite dans la galerie publiée avec cette version. Les captures sont prises avec Minecraft, sans retouche. La partie de validation utilise un profil de démonstration niveau 20 pour vérifier les achats et la synchronisation ; ce profil ne fait pas partie du mod distribué.

## Installer

Sauvegarder sa partie, remplacer l’ancien JAR TND dans `mods/` par `tnd-0.3.0-dev.jar` et conserver une seule version du mod. Les maîtrises et les interactions fonctionnent dans une sauvegarde existante. Il n’est pas nécessaire de recommencer son monde. Explorer de nouveaux chunks pour voir les ajouts de consoles dans les archives et les sanctuaires ; les consoles déjà présentes dans les autres ruines sont utilisables immédiatement.
