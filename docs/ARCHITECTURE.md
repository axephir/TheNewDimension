# Architecture et décisions

## Choix retenus

La demande explicite de NeoForge 26.2 remplace la proposition Forge 1.20.1 du document. Le MDK officiel 26.2 référence Minecraft 26.2, NeoForge 26.2.0.93, Java 25, ModDevGradle 2.0.148 et Gradle 9.2.1. Ces versions sont épinglées. Pas de bibliothèque de gameplay tierce.

Le nom de travail est The New Dimension, espace de noms `tnd`, dimension `tnd:vestige`. Les noms des biomes et ressources sont provisoires. Les deux premières espèces sont des propositions documentées dans ENCOUNTERS_0.4.0.md. Le premier boss et la transformation industrielle sont documentés dans EXPEDITION_0.7.0.md. Les autres boss, l’énergie industrielle avancée et l’accès quadridimensionnel restent à concevoir.

## Séparation des responsabilités

| Package | Responsabilité |
|---|---|
| `domain` | XP, talents, profils immuables, cycle, calcul de production ; aucune dépendance Minecraft |
| `core` | Enregistrements et configuration serveur |
| `world` | Quête d’accès, détecteur, passages et commandes de développement |
| `progression` | Événements manuels, attachements persistants, perception et menu autoritaire |
| `technology` | Extracteur et forge : inventaires, production différée, menus, entonnoirs et pause redstone |
| `client` | Écrans et configuration graphique, chargés uniquement sur le client |
| `entities` | Familiers, sentinelles, boss lié au sanctuaire et transport interdimensionnel |
| `dungeon` | Sceaux, épreuves finies, contrôleur persistant et égide défensive |
| `gametest` | Tests d’intégration natifs : monde, portails, progression, consoles et machines |

Les modules narratifs et anomalies ne sont pas représentés par des classes vides : ils seront ajoutés avec leur première fonctionnalité réelle.

## Autorité et persistance

Le serveur attribue l’expérience et achète les talents. Un client envoie seulement un index de bouton via le protocole de menu vanilla ; le serveur vérifie le menu, la maîtrise, les points, le rang et les prérequis. Les profils sont immuables et synchronisés uniquement à leur propriétaire avec les attachements NeoForge. `copyOnDeath` conserve la progression après un décès.

Le codec de profil possède un numéro de schéma, limite la taille, rejette les doublons et les points incohérents. Une donnée invalide n’est pas réinitialisée silencieusement. Une migration explicite sera nécessaire si le schéma change. Les XP utilisent des entiers longs avec saturation protégée contre le débordement ; la progression dépasse largement le niveau 100, mais ne prétend pas offrir une capacité arithmétique infinie.

Les ressources placées par les joueurs sont mémorisées par chunk pour ne pas redonner d’XP lors de leur récolte répétée. Les actions mécaniques ne déclenchent pas les récompenses manuelles. La résistance aux déplacements par pistons et aux interactions avec d’autres mods doit encore être testée et renforcée ; elle n’est pas annoncée comme étanche.

Un historique immuable limité à 2 048 entrées conserve les budgets d’activité et les territoires récents. Il est sauvegardé côté serveur sans être synchronisé au client. Les nouveaux identifiants de maîtrise réutilisent le codec existant : aucune remise à zéro des XP ou des talents.

## Industrie

Le moteur `Production` transforme une quantité finie de carburant en une quantité bornée de ressources. Il est déterministe, idempotent à temps constant et ne remplit pas rétroactivement un inventaire bloqué. Le temps pris en compte est celui du serveur, avec un maximum configurable par rattrapage ; un serveur arrêté ne produit rien.

L’extracteur règle son compte avant une mutation d’inventaire ou un recalibrage, et sauvegarde son horloge, son travail partiel et ses objets. Les bonus d’ingénierie sont copiés lors de l’interaction manuelle, afin de continuer à fonctionner quand le joueur se déconnecte. Le rattrapage se fait au retour du chunk, sans forcer son chargement pendant l’expédition.

L’extracteur possède cinq profils persistants : fer brut, cuivre brut, or brut, redstone et diamant. Les durées relatives sont 1, 1, 3, 2 et 8. Le diamant exige un module de stabilisation. Chaque objet consomme un charbon. La sortie doit être vide pour changer de profil ; la progression partielle est alors remise à zéro. La calibration de relique est distincte de celle de l’ingénieur, ne diminue pas lors d’une visite et survit au rechargement. Les valeurs sont ajustables. Un réseau d’énergie et les autres machines ne sont pas simulés par des objets décoratifs.

## Monde et accès

La génération repose sur des registres de données natifs : densité 3D originale, biomes, minerais, pools de structures et placement `random_spread`. Pour les portails, l’espacement de 768 chunks et la séparation de 640 chunks sont des réglages provisoires visant l’ordre de grandeur du document ; la distribution réelle doit être mesurée sur plusieurs seeds.

Le détecteur appelle la recherche native de structure. Il ne calcule pas de coordonnées fictives. Les six modèles de ruines sont originaux, fermés et aménagés. L’avant-poste de départ est demandé uniquement lors de la création du spawn d’un nouveau monde et placé à la première connexion ; les mondes existants ne sont pas automatiquement modifiés par ce mécanisme.

Le passage du prototype s’utilise en cliquant son mécanisme. L’animation et la traversée automatique d’une surface de portail restent à compléter. Les couples sont sauvegardés en `GlobalPos`. L’arrivée doit être dégagée ; le code refuse de supprimer des constructions pour placer un passage retour. Les déplacements de familiers concernent actuellement les animaux vanilla apprivoisés compatibles ; les Éclats errants utilisent aussi les changements de dimension vanilla, avec capture avant transfert puis recherche d’un sol sûr après réussite. Aucun déplacement n’a lieu sur un transfert annulé.

## Conclusion et récupération — conception, pas fonctionnalité livrée

Le document exige de développer la catastrophe tardivement. Aucun appel d’explosion scénaristique n’est présent dans le gameplay. Le seul appel d’explosion est contenu dans le GameTest de résistance, exécuté dans le monde de test séparé.

Avant une implémentation, la solution devra préserver les données sources, créer une représentation isolée des destructions et séparer les inventaires temporaires. La copie ne devra jamais s’exécuter dans les chemins du monde d’origine. Un journal transactionnel durable devra distinguer préparation, vérification, exploration et retour. Le retour restera accessible indépendamment de l’interface. Aucune copie de récupération ne sera supprimée automatiquement tant que la validation finale n’aura pas réussi.

La politique multijoueur demeure ouverte : déclenchement administré, consentement des joueurs concernés et isolation d’une session sont à décider. Un test de disque plein, corruption, arrêt brutal à chaque transition, déconnexion et redémarrage sera indispensable. Cette description ne constitue ni une preuve de sûreté ni une sauvegarde implémentée.

Le sanctuaire de résonance utilise un seul contrôleur de données, trois sceaux ordinaires et au plus trois UUID de gardiens actifs. Le marquage naturel provient du modèle NBT ; les objets reposés ne le transportent pas. Les morts confirmées et le jeton de vague font autorité, sans déduire une victoire de l’absence d’une entité. Rotations et miroirs transforment à la fois les positions et le repère des mécanismes.

La 0.6 utilise six cellules climatiques à deux axes et mélange leurs densités aux frontières. Les éléments de paysage travaillent dans le seul chunk décoré et vérifient leur volume avant écriture. Les sons de biome suivent le système natif ; un contrôleur exclusivement client ajoute au plus deux nappes contextuelles et deux événements ponctuels, avec arrêt au changement de dimension. Les célébrations de maîtrise sont émises par le serveur lors du franchissement du seuil, sous forme de paquets natifs adressés au propriétaire.

La 0.7 conserve les trois sceaux et leur trésor lors de la migration. Le boss optionnel dispose d’un UUID et d’un jeton distincts dans le contrôleur ; les anciens champs restent inchangés. Son état d’attaque est serveur, ses tracés sont des particules natives et ses seuls champs visuels sont synchronisés. La récompense globale est marquée récupérée avant le transfert des objets. La forge règle les cycles avant mutation des emplacements, persiste sa pause et bloque les mises à jour de lumière pendant sa suppression. Ses calculs bornent simultanément temps, combustible, matière et sortie.

La 0.8 ajoute des notes d’expédition immuables et bornées dans un attachement distinct, conservé après mort et synchronisé au propriétaire. Les menus ne reçoivent qu’un indice de récompense ; les conditions et objets sont déterminés côté serveur. La mort confirmée d’une sentinelle libre enregistre l’objectif après acceptation par le moteur. Le retour utilise une identité persistante de bloc, une liaison sur le prisme, cent mises à jour natives d’utilisation et une vérification finale de l’arrivée avant prélèvement. Aucune canalisation n’est sauvegardée. Le pupitre naturel est marqué dans le NBT de l’observatoire ; les objets reposés ne portent pas cette autorité.

La 0.9 réutilise le même processeur fini pour forge et séparateur ; leurs recettes, périodes et menus sont distincts. Le récolteur observe au plus 24 emplacements voisins chargés, conserve les huit charges d’un charbon, valide capacité et maturité avant toute mutation, et n’accorde ni XP ni progression automatique. Il n’utilise aucun rattrapage temporel. Les nouveaux minerais sont générés après le paysage, dans le chunk courant, uniquement sur des sols exposés et naturels. Les deux nouveaux types d’entités partagent les contrôles d’attaque mais possèdent silhouettes, timings, butins et habitats distincts ; seules charge et récupération sont synchronisées, les animations et l’aide visuelle demeurant locales. Le guide ne détient pas d’autorité serveur. Les identifiants et schémas d’attachement de la 0.8 sont conservés.
