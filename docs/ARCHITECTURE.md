# Architecture et décisions

## Versions et modules

La demande NeoForge 26.2 remplace la proposition Forge 1.20.1 du cahier initial. Versions épinglées : Minecraft **26.2**, NeoForge **26.2.0.93**, Java **25**, ModDevGradle **2.0.148**, Gradle **9.2.1**. Aucune bibliothèque de gameplay supplémentaire.

| Package | Responsabilité |
|---|---|
| `domain` | Calculs indépendants du jeu : profils, XP, talents, budgets, production, cycle et expéditions |
| `core` | Enregistrements et configuration |
| `world` | Génération, structures couronnées, accès, portails et vérification des arrivées |
| `city` | Biome urbain continu, relief commun, génération par chunk, dôme, protection, sécurité et archives |
| `progression` | Attachements, événements manuels, provenance des blocs, talents et perception |
| `technology` | Transformations finies, énergie, réseau, carrière, agriculture, élevage et maintien des ateliers |
| `entities`, `familiar` | Créatures, attaques, boss, lien unique, incubation, états blessés, niveaux et transport |
| `dungeon`, `expedition` | Épreuves, récompenses finies, objectifs, observatoire et retour canalisé |
| `narrative` | Références durables, projections personnelles, récupération et perception finale |
| `client` | Écrans, modèles, rendu, effets GPU et audio local ; jamais chargé par le serveur dédié |
| `gametest` | Intégration native dans un monde distinct des parties normales |

## Serveur et persistance

Le serveur décide des XP, talents, recettes, ressources, propriétaires, destinations et états de combat. Le client transmet des indices de menu ou des choix bornés ; le serveur vérifie le propriétaire, le menu courant, ses prérequis et la distance pertinente. Les attachements de progression se synchronisent à leur propriétaire et survivent à la mort.

Les profils sont immuables et possèdent un schéma. Leurs codecs rejettent les incohérences au lieu de réinitialiser silencieusement les données. L’XP utilise des entiers longs avec saturation ; la progression dépasse le niveau 100 sans prétendre offrir des entiers mathématiquement infinis. Les budgets d’activité sont persistants et bornés.

La provenance des minerais et autres ressources posées est stockée par chunk. Le déplacement réussi d’une chaîne de piston transfère ces marques, y compris entre chunks ; un déplacement annulé ne les change pas. Les machines n’attribuent pas d’XP manuelle.

Le registre mondial des familiers attribue un lien unique par UUID de joueur. La capsule contient une identité et un ticket révocable ; le modèle NBT central fait autorité. Le déploiement vérifie la position, le sol, les dangers, la collision et l’absence du même UUID dans toutes les dimensions chargées avant de consommer le ticket. La réémission invalide les copies anciennes.

## Génération et passages

Les neuf anciens sites sont des pièces de structure sauvegardées, découpées par chunk, avec massif, pics, couronne, accès et bâtiment. Les passages de l’Overworld utilisent un placement natif rare et une recherche de structure réelle. Le moniteur vérifie son assemblage et ses 16 000 FE, canalise l’ouverture et n’établit le couple permanent qu’après validation de l’arrivée. Le passage ne supprime jamais de constructions pour dégager la destination.

Les métropoles utilisent un champ spatial partagé par `CityBiomeSource`, la fonction de densité, la génération et la protection. Les centres sont décalés sur une grille de 3 072 blocs et le rayon est de 640 blocs. Ce champ est indépendant de la seed ; le terrain naturel autour reste généré par le bruit du monde. L’alignement exact évite des limites de biome qui coupent les rues et le dôme.

Chaque feature écrit uniquement dans le chunk décoré. Les tours sont des tuiles NBT, avec rotation et hauteur adaptées à la voûte. Un seul emplacement de laboratoire et d’ordinateur appartient à chaque région, indépendamment de l’ordre de chargement. Un marqueur de génération permet de préserver les anciennes bases non urbaines sous la nouvelle grille. Les anciennes structures de quartier restent enregistrées pour les sauvegardes, mais ne servent plus à générer les nouvelles métropoles.

## Production et charge des ateliers

L’extracteur et les processeurs finis règlent les cycles avant mutation d’inventaire. Matière, carburant, sortie et rattrapage sont bornés. Un serveur arrêté ne produit rien. Le réseau FE parcourt uniquement des chunks chargés, visite au plus 128 positions et utilise les transactions natives ; câbles et accumulateurs ne créent pas d’énergie.

Les machines électriques réalisent leur travail dans le monde : récolte, replantation, nourriture, animaux, fluides, trépans et sorties sont vérifiés. La carrière exige un montage et un démarrage explicites ; elle respecte les refus des protections de blocs.

Une ancre entretient neuf chunks contre un coût de 16 FE/t, sous réserve d’énergie, de montage, de redstone et d’un propriétaire connecté. Deux sites au plus par joueur, seize actifs par serveur par défaut. Son contrôleur ne force pas les apparitions naturelles. Les tickets persistants de ce seul contrôleur sont retirés au chargement, puis les sites du joueur sont revalidés à sa connexion. Rupture et déconnexion libèrent les tickets. Les extracteurs conservent leur mécanisme distinct de production différée.

## Conclusion isolée

`Convergence` gère préparation, entrée, caméra, exploration, signal, retour et conclusion. Le choix multijoueur est personnel : aucun retour ne rembobine les actions d’un autre joueur.

`EchoCopyTask` refuse toute destination autre que `tnd:aftermath`. Il lit les blocs sources, construit un chunk de projection par mise à jour, crée uniquement des conteneurs visuels vides et remplace les machines par des débris inertes. Les heightmaps et la lumière sont recalculées avant l’entrée. La zone fait 17 × 17 chunks par défaut ; la réservation durable empêche sa réutilisation après perte de l’index. Les effets d’explosion de la caméra sont des particules et des sons émis dans cette seule dimension.

`EchoReference` capture toutes les cases d’inventaire, armure et seconde main comprises, le coffre de l’Ender, XP, nourriture, santé et effets. La capture est refaite et vérifiée au passage effectif vers la projection, pour ne pas rétablir un inventaire périmé après la préparation. La projection impose l’observation native ; un message distinct permet seulement les choix scénaristiques vérifiés.

`EchoJournal` conserve deux fichiers compressés et deux empreintes SHA-256. Chaque écriture utilise un fichier temporaire, `force(true)`, un renommage atomique et une seconde synchronisation du fichier final. Le répertoire est également synchronisé sur les systèmes qui exposent ce canal ; Windows ne le permet pas. Les erreurs d’écriture des fichiers restent bloquantes sur tous les systèmes. L’entrée est refusée si les deux références ne sont pas vérifiées. Un repère sur le joueur, un index SavedData et la recherche des journaux du propriétaire permettent une récupération indépendante de l’interface. Un retour terminé ne peut pas être rejoué pour rétablir un ancien inventaire. Aucun journal n’est supprimé automatiquement.

Au retour, le sol est vérifié sans supprimer de bloc. Le monde source n’est jamais une destination de copie ou de destruction. Reconnexion et `/tnd recover` prennent en charge une session interrompue. Si les références sur disque sont invalides, l’inventaire natif resté intact en observation constitue une voie de repli ; les fichiers endommagés restent conservés.

Le client conserve au plus une texture GPU de la trame précédente pour une rémanence légère. Il n’effectue aucune lecture des pixels vers le processeur, libère la texture en quittant l’effet et désactive ce rendu si l’allocation échoue. Les réglages d’accessibilité peuvent le retirer et passer la rotation cinématique. Les projections tétrales sont des hypercubes tournant sur deux plans avant projection en trois dimensions.

## Validation et portée

`build` exécute le noyau indépendant. `runGameTestServer` vérifie les systèmes dans `run-gametest/`, séparé des parties client et serveur habituelles. Les registres de datapack sont décodés par Minecraft. Les fixtures de dimensions des tests utilisent les types natifs ; la génération complète et l’affichage se vérifient également dans le client.

Les tests de corruption et d’écriture ne démontrent pas l’absence de toute panne matérielle. L’isolation des données sources reste la protection principale, conformément au §14.6 du cahier. Les vérifications et comportements de la version sont détaillés dans [SYMBIOSIS_0.12.0.md](SYMBIOSIS_0.12.0.md) et [CAHIER_COUVERTURE_0.12.md](CAHIER_COUVERTURE_0.12.md).
