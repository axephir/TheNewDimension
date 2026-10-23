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

`EchoCopyTask` refuse toute destination autre que `tnd:aftermath`. Il lit les blocs sources, construit un chunk de projection par mise à jour, crée uniquement des conteneurs visuels vides et remplace les machines par des débris inertes. Les heightmaps et la lumière sont recalculées avant l’entrée. La zone fait 25 × 25 chunks par défaut dans les nouvelles configurations ; la réservation durable empêche sa réutilisation après perte de l’index. Les impacts de caméra retirent des blocs uniquement dans cette copie, avec particules et sons dans cette seule dimension.

`EchoReference` capture toutes les cases d’inventaire, armure et seconde main comprises, le coffre de l’Ender, XP, nourriture, santé et effets. La capture est refaite et vérifiée au passage effectif vers la projection, pour ne pas rétablir un inventaire périmé après la préparation. La projection impose l’observation native ; un message distinct permet seulement les choix scénaristiques vérifiés.

`EchoJournal` conserve deux fichiers compressés et deux empreintes SHA-256. Chaque écriture utilise un fichier temporaire, `force(true)`, un renommage atomique et une seconde synchronisation du fichier final. Le répertoire est également synchronisé sur les systèmes qui exposent ce canal ; Windows ne le permet pas. Les erreurs d’écriture des fichiers restent bloquantes sur tous les systèmes. L’entrée est refusée si les deux références ne sont pas vérifiées. Un repère sur le joueur, un index SavedData et la recherche des journaux du propriétaire permettent une récupération indépendante de l’interface. Un retour terminé ne peut pas être rejoué pour rétablir un ancien inventaire. Aucun journal n’est supprimé automatiquement.

Au retour, le sol est vérifié sans supprimer de bloc. Le monde source n’est jamais une destination de copie ou de destruction. Reconnexion et `/tnd recover` prennent en charge une session interrompue. Si les références sur disque sont invalides, l’inventaire natif resté intact en observation constitue une voie de repli ; les fichiers endommagés restent conservés.

Le client conserve au plus une texture GPU de la trame précédente pour une rémanence légère. Il n’effectue aucune lecture des pixels vers le processeur, libère la texture en quittant l’effet et désactive ce rendu si l’allocation échoue. Les réglages d’accessibilité peuvent le retirer et passer la rotation cinématique. Les projections tétrales sont des hypercubes tournant sur deux plans avant projection en trois dimensions.

## Validation et portée

`build` exécute le noyau indépendant. `runGameTestServer` vérifie les systèmes dans `run-gametest/`, séparé des parties client et serveur habituelles. Les registres de datapack sont décodés par Minecraft. Les fixtures de dimensions des tests utilisent les types natifs ; la génération complète et l’affichage se vérifient également dans le client.

Les tests de corruption et d’écriture ne démontrent pas l’absence de toute panne matérielle. L’isolation des données sources reste la protection principale, conformément au §14.6 du cahier. Les vérifications et comportements de la version sont détaillés dans [TRACES_0.13.0.md](TRACES_0.13.0.md) et [CAHIER_COUVERTURE_0.13.md](CAHIER_COUVERTURE_0.13.md).


## Spécialisations et distinctions (0.13)

Les cinq identités historiques de `Talent` sont conservées. Trente identités spécialisées ajoutent un propriétaire de maîtrise, un niveau minimum et une dépendance. `MasteryProgress` contrôle rangs, points et niveaux ; `Profile` refuse un talent appartenant à une autre maîtrise. Le protocole ne transmet qu’un index, jamais un montant d’XP ou un rang arbitraire.

`MasteryAbilities` contrôle contexte, inventaire, mode de jeu et recharge serveur commune. La replantation est différée au tick suivant : l’événement de butin intervient avant que vanilla retire le bloc récolté. La file est bornée à 256 opérations et revérifie sol, air, joueur, dimension et protection urbaine avant de prendre une graine. Les effets ne produisent aucune XP. Le titre et le masque de récompenses `Prestige` sont persistants ; leur sélection est validée avant toute remise de sceau.

Les anomalies choisissent une origine dans les seuls chunks déjà chargés. La réaction réelle d’une porte exige le marqueur d’un quartier nouvellement généré et sa protection urbaine, ne touche pas une porte alimentée et ne fait qu’ouvrir. Les autres phénomènes restent des perceptions locales.

L’examen des ruines n’accepte aucune coordonnée client. Il utilise le rayon du joueur à six blocs, sa session personnelle et une recharge de trente ticks. Les quatre traces sont synchronisées vers leur propriétaire. Aucun bloc, inventaire ou niveau du monde source n’est modifié par ces interactions.

## Ciblage personnel — 0.13.1

`PlayerBase` conserve la provenance des équipements via les attachments serveur `utility_placer` et `base_utilities`. Un placement natif réussi attribue le bloc ; aucune recherche ne déduit le propriétaire d’un inventaire généré. L’index personnel est borné, sérialisé et copié à la mort. Le contrôleur inspecte au plus un chunk indexé par tick près du lit personnel, puis choisit un groupe compact d’au moins dix équipements valides. `EchoCopyTask` distingue le noyau de la scène, son foyer visuel et le noyau réel de référence : seule la copie centrée sur le lit est ciblée. Le signal persistant permet de retrouver le noyau relocalisé sans modifier le format des anciennes références de retour. Les impacts et le dégagement vertical des débris sont bornés aux chunks copiés. [Détails et limites](BASE_CIBLEE_0.13.1.md).

## Tours de confinement — 0.14

`AssaultNodeEntity` initialise des rencontres finies dans les seuls chunks de métropole marqués. Chaque insertion réussie incrémente un compteur sauvegardé : quatre gardes par étage ou un mini-boss de sommet. Aucun mob absent n’est présumé mort et aucun rechargement ne réapprovisionne le compteur. Les recherches de collision, populations et luminaires restent locales aux chunks chargés. La panne des seuls `assault_lamp` est temporaire et son délai est sauvegardé.

Les trois nouvelles valeurs de `CityMob.Kind` s’ajoutent aux identités existantes ; les trois types naturels restent les seuls inscrits aux biomes. Les mini-boss réutilisent les déplacements avec collisions, les visées figées, les avertissements et les fenêtres de récupération. Le Chirurgien soigne au plus six alliés visibles, le Traqueur est interruptible et contourne sa cible, le Maréchal a une échelle physique et visuelle accrue.

Les tables natives de chaque mini-boss possèdent un unique pool d’armure à probabilité fixe de 0,2 et quatre entrées de même poids. Les récompenses de mémoire et de processeurs sont séparées. Les douze pièces ont 500 de durabilité et sont inscrites aux tags d’enchantabilité vanilla. `AssaultEquipment` entretient les effets des ensembles complets côté serveur. Le restaurateur est ajouté en fin d’énumération industrielle pour préserver les indices historiques ; son opération copie les composants et consomme les entrées après contrôle de l’unique sortie et du coût FE. Les protocoles du familier sont trois bits persistants, validés par le lien propriétaire et reproduits dans le dossier.


## Constellations 0.15

`MasteryScreen` remplace la liste de boutons par sept nœuds par maîtrise, reliés depuis `Talent.prerequisite()`, et quatre nœuds de distinctions. `ConstellationStyle` dessine les panneaux, étoiles et connexions avec les primitives GUI natives et des icônes d’objets vanilla/TND. La vue est recadrée au redimensionnement ; le zoom conserve la position du monde sous le pointeur, les nœuds restent lisibles aux faibles grossissements et le scissor borne le rendu et les clics au canevas. La fiche latérale défile indépendamment et les commandes restent fixes ; une disposition horizontale prend le relais dans les petites fenêtres.

Sélection et achat sont séparés. Les paquets `MasteryMenu` et la validation serveur restent inchangés. Les confirmations observent le profil synchronisé, sans achat optimiste ni modification de points côté client. Le son est sur `SoundSource.UI`, le survol reste silencieux et `AtmosphereOptions.ANIMATIONS` borne les mouvements. La préférence de consultation est limitée à quinze maîtrises en mémoire client ; elle ne modifie aucune sauvegarde. `constellation_resources.languages()` reste intégré à la génération bilingue et les références littérales de son helper sont contrôlées par le validateur.

## Interfaces astrales 0.15.1

`AstralPanel` remplace les surfaces de `StonePanel` par les primitives de `ConstellationStyle` : contours, fond spatial borné par scissor, cartes opaques et emplacements natifs contrastés. Le champ d’étoiles utilise un nombre borné de rectangles et une variation lente d’opacité ; aucune entité de particule ni cible de rendu supplémentaire n’est créée. `AtmosphereOptions.ANIMATIONS` fige ce fond et désactive les interpolations de `PanelMotion`.

`AstralButton` conserve le comportement natif de `Button`, la narration, le focus et les callbacks existants. Il centralise bordures, état sélectionné, indisponibilité, ajustement des libellés et clic discret sur le canal UI. Le thème s’applique aux treize écrans du mod, aux raccourcis d’inventaire et au bouton Familier des paramètres ; aucune modification globale des widgets vanilla n’est enregistrée. Les coordonnées des slots et les messages serveur ne changent pas.

`armor_icons.py` contient les palettes RGBA et les grilles 16 × 16 des seize icônes d’équipement. `awakening_art.paint` et `assault_resources.content` utilisent cette source canonique lors des régénérations. Elle ne produit que `textures/item/` : les atlas d’équipement porté et les modèles sont indépendants. [Présentation](ASTRAL_0.15.1.md).

## Observatoire du Premier Silence — 0.16

`SanctuaryBiomeSource` enveloppe la source existante sans remplacer ses biomes. Une région de 1 280 × 1 024 blocs est réservée par cellule de 6 144 blocs. `SanctuaryBlend` adapte la densité du terrain à son socle ; `SanctuaryFeature` écrit exclusivement dans son chunk, marque sa génération et construit les 48 chambres, passages, voûtes, mobilier, réserves et couronnes. Les protections ne s'appliquent qu'aux chunks réellement marqués, jamais aux constructions anciennes situées sur la même grille.

`SanctuaryRoute` et `SanctuaryPuzzles` portent les règles pures : coordonnées négatives, préfixe de progression et six familles d'accords couplés. `SanctuaryLedger` sauvegarde l'avancement par site et les masques personnels d'éligibilité/récompenses. Une station exige sa position exacte, son chunk marqué et son instance vivante pour agir. Le menu transmet une action et éventuellement un index ; distance, dimension et prérequis sont revérifiés sur le serveur. Le masque de 48 bits utilise trois champs de 16 bits.

Chaque tentative de combat possède un jeton et un ensemble borné d'UUID. Une mort native retire l'UUID et compte une victoire ; une absence ne la remplace pas. Les anciennes entités se révoquent après une réinitialisation. Les trois vagues ne se chevauchent pas. Les participants de la tentative précédente sont effacés avant une reprise. Les boss gardent une réserve d'intégrité finie au-delà de la limite native de 1 024 PV, sans modifier les attributs globaux ; seules leurs dernières couches appellent la mort et la récompense. Les télégraphes figent leurs coordonnées, respectent les lignes de vue et ne modifient pas le terrain.

Les nouvelles machines sont ajoutées à la fin de `Industry.Kind` pour préserver les identités historiques. `ObservatoryProcessing` décrit explicitement les trois conversions. Le trieur conserve exactement les composants de l'objet, garde son filtre et interdit son insertion automatique par les hoppers. Le socle, la place de sortie, les entrées et l'énergie sont vérifiés avant la transaction ; aucun cycle ne travaille hors chargement. Le quatrième protocole du familier utilise le bit 8 sans déplacer les trois bits existants.

Le client utilise les panneaux et boutons astrals, des modèles articulés avec couches émissives, et une ambiance locale fondue. Les réglages existants restent applicables. Les atlas et modèles des armures portées de la 0.15.1 sont conservés. Les générateurs canoniques, les GameTests et le guide de la version décrivent les nouveaux contrats ; la durée d'exploration reste une cible de playtest.

Les quatre positions des disques sont synchronisées séparément de la cible, pour permettre la lecture des accords couplés. Réserve et intégrité maximale sont aussi synchronisées : le HUD ne présente pas une rupture de couche comme un soin du boss. La désinscription d'un observateur supprime sa barre de boss même si la dimension cesse de simuler ses entités.

### Intérieurs 0.16.1

`SanctuaryInterior` décrit les sections locales de l’Observatoire, y compris les sols négatifs des cours excavées. `SanctuaryFeature` échantillonne aussi les fondations : aucune couche plane ne recouvre les escaliers enterrés. Les mécanismes, couloirs et coordonnées de stations restent ceux de `SanctuaryRoute`. Les réacteurs disposent de semelles pleines, les terrasses et galeries de marches natives, et les boss d’un volume central dégagé. Deux GameTests supplémentaires vérifient les accès dans les 48 salles à partir des formes de collision natives et contrôlent une excavation réellement générée, sans écrire dans les chunks voisins. La révision s’applique aux nouvelles générations ; aucun remplacement automatique de salles sauvegardées.

### Premier acte 0.17

`WitnessAct` contient les positions et règles pures des trois coupleurs, trois découvertes et dix étapes de passerelle. `WitnessArchitecture` les intègre aux échantillons locaux sans lecture des chunks voisins. `WitnessScene` valide l’origine naturelle, la portée, la visibilité et la position exacte de chaque interaction ; ses mutations restent limitées à des coordonnées et matériaux autorisés dans des chunks chargés. La passerelle est monotone, s’arrête devant une obstruction et reprend son étape sauvegardée.

Le marqueur `witnessAct` est attribué aux nouvelles stations du premier acte ; son absence conserve les anciens objectifs. La progression de route reste commune, les trois récompenses facultatives sont personnelles et distinctes des six récompenses de boss. Les appareils n’introduisent aucun nouveau bloc entité : la station porte les petits états persistants. Les parties décoratives tournent à 4 Hz, les faisceaux à 2 Hz et les gravures à 1 Hz, avec écritures uniquement lors de changements et suspension sans joueur à proximité. Aucun terrain n’est modifié pendant les phases du Colosse, hormis l’état lumineux des gravures.
