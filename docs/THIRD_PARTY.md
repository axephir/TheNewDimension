# Sources et licences

- MDK officiel : https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle, révision observée `a0340c5454c01e43ccf83977601a4d90afee6e1f`. Les scripts wrapper et les conventions de construction proviennent de ce modèle. Sa licence MIT est conservée dans `TEMPLATE_LICENSE.txt`.
- Le JAR wrapper Gradle a été remplacé par le wrapper officiel **v9.2.1**, puis comparé à la somme SHA-256 publiée par Gradle : `423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9`. Gradle est distribué sous Apache-2.0 : https://github.com/gradle/gradle/blob/v9.2.1/LICENSE.
- Distribution Gradle 9.2.1 : somme SHA-256 épinglée dans `gradle-wrapper.properties`, provenant de https://services.gradle.org/distributions/gradle-9.2.1-bin.zip.sha256.
- JDK cloud : Temurin 25.0.4.1+1, archive officielle Adoptium, SHA-256 vérifiée contre le fichier publié avec la version : `dbb698396d478e7fa2b1e50f4103324b2a99b90569ee27c33f2261f9215cf41e`. Le JDK reste hors du dépôt et conserve ses licences et certificats fournisseurs.
- APIs consultées : sources NeoForge `26.2.x`, révision `4e2117162e07bdf22de7e705a26fa0a0ae382350`, et documentation officielle https://github.com/neoforged/Documentation/blob/main/docs/misc/gametest.md. La compilation et les GameTests ont été exécutés avec les versions épinglées.
- Formats de données vérifiés dans les données Minecraft 26.2 : https://github.com/misode/mcmeta/tree/26.2-data, révision `4d12c0553e21e461085d08dcb2c5d412398c494e`. DataVersion 4903, format de données 107.1 et format de ressources 88.0. Les réglages de génération, textures, structures et le son de ce dépôt sont originaux ; aucun ensemble d’assets Minecraft n’a été recopié.

Le code et les créations originales restent sous **All Rights Reserved**, conformément au choix initial du modèle. Aucune licence de publication définitive n’a été choisie à la place de l’auteur. Minecraft, NeoForge et leurs marques appartiennent à leurs titulaires respectifs ; les fichiers Minecraft téléchargés par Gradle ne sont pas redistribués dans ce dépôt.

Les dix-huit sons de la 0.6 sont synthétisés par `scripts/soundscape_audio.py` (oscillateurs, bruit filtré et enveloppes), puis encodés par ffmpeg. Aucun échantillon vocal, morceau de musique ou enregistrement tiers n’est incorporé.

Les seize effets de la 0.9 sont synthétisés par `scripts/awakening_audio.py` avec des oscillateurs, bruit filtré et enveloppes propres, puis encodés par ffmpeg. Les modèles articulés, les textures de blocs et d’icônes 16 × 16, les atlas de personnages 64 × 64, les massifs de scorie et les deux nouvelles architectures sont également des créations originales du projet.

Les seize icônes d’armure de la 0.15.1 proviennent d’une création originale assistée par génération d’image, exportée en sprites 16 × 16 à palette limitée. Les grilles et palettes finales figurent dans `scripts/armor_icons.py` pour une reproduction indépendante du générateur d’image. Aucun sprite d’armure Minecraft ni ressource d’un pack tiers n’a été recopié. Les modèles et atlas portés sont conservés. Les surfaces astrales sont dessinées avec les primitives GUI et les sons de clic utilisent l’événement natif de Minecraft, sans redistribuer son enregistrement.

Les seize textures de blocs et les neuf nouvelles icônes d’objets de l’Observatoire 0.16 proviennent de créations originales assistées par génération d’image, exportées en 16 × 16 à palette limitée et transparence franche. Les grilles et palettes canoniques figurent dans `scripts/sanctuary_art.py` et `scripts/sanctuary_item_art.py`. Les atlas des gardiens, modèles, géométrie et fragments narratifs sont originaux. Les sept nouveaux sons sont synthétisés par `scripts/sanctuary_audio.py`, sans échantillon tiers.

Références visuelles externes consultées pour l’atmosphère de l’Observatoire d’Assassin’s Creed Black Flag Resynced : [salle d’observation](https://www.noobfeed.com/articles/assassins-creed-black-flag-resynced-observation-room) et [sortie de l’Observatoire](https://www.noobfeed.com/articles/assassins-creed-black-flag-resynced-exit-observatory). Aucune capture ni ressource Ubisoft n’est incorporée aux fichiers distribués.

## Révision 0.16.1

Les neuf sprites d’objets du Premier Silence ont été redessinés avec l’outil de génération d’images, puis extraits, recadrés et exportés techniquement en 16 × 16. Leurs palettes et pixels canoniques sont dans `scripts/sanctuary_item_art.py`. Aucun sprite d’un autre jeu ni texture de référence Assassin’s Creed n’est distribué.

## Révision 0.17

Les appareils du premier acte utilisent des modèles JSON originaux et la bibliothèque de textures 16 × 16 du projet. Les quatre nouveaux sons sont synthétisés par `scripts/witness_audio.py`, sans enregistrement externe, et possèdent leurs sous-titres français et anglais. Les 65 fichiers sonores précédents ne sont pas régénérés pour cette révision.


## Révision 0.18

La forge conserve les matériaux originaux de pierre noire en 16 × 16. La plaque graduée des vannes, le sprite du cœur et les cinq atlas de personnages sont des créations procédurales originales, définies dans `scripts/foundry_resources.py`. Modèles articulés, dispositifs JSON, plans, textes et interfaces sont originaux. Les cinq nouveaux sons sont synthétisés par `scripts/foundry_audio.py`, sans enregistrement externe ; les 69 OGG précédents sont conservés byte pour byte.
