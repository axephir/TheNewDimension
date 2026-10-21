# Sources et licences

- MDK officiel : https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle, révision observée `a0340c5454c01e43ccf83977601a4d90afee6e1f`. Les scripts wrapper et les conventions de construction proviennent de ce modèle. Sa licence MIT est conservée dans `TEMPLATE_LICENSE.txt`.
- Le JAR wrapper Gradle a été remplacé par le wrapper officiel **v9.2.1**, puis comparé à la somme SHA-256 publiée par Gradle : `423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9`. Gradle est distribué sous Apache-2.0 : https://github.com/gradle/gradle/blob/v9.2.1/LICENSE.
- Distribution Gradle 9.2.1 : somme SHA-256 épinglée dans `gradle-wrapper.properties`, provenant de https://services.gradle.org/distributions/gradle-9.2.1-bin.zip.sha256.
- JDK cloud : Temurin 25.0.4.1+1, archive officielle Adoptium, SHA-256 vérifiée contre le fichier publié avec la version : `dbb698396d478e7fa2b1e50f4103324b2a99b90569ee27c33f2261f9215cf41e`. Le JDK reste hors du dépôt et conserve ses licences et certificats fournisseurs.
- APIs consultées : sources NeoForge `26.2.x`, révision `4e2117162e07bdf22de7e705a26fa0a0ae382350`, et documentation officielle https://github.com/neoforged/Documentation/blob/main/docs/misc/gametest.md. La compilation et les GameTests ont été exécutés avec les versions épinglées.
- Formats de données vérifiés dans les données Minecraft 26.2 : https://github.com/misode/mcmeta/tree/26.2-data, révision `4d12c0553e21e461085d08dcb2c5d412398c494e`. DataVersion 4903, format de données 107.1 et format de ressources 88.0. Les réglages de génération, textures, structures et le son de ce dépôt sont originaux ; aucun ensemble d’assets Minecraft n’a été recopié.

Le code et les créations originales restent sous **All Rights Reserved**, conformément au choix initial du modèle. Aucune licence de publication définitive n’a été choisie à la place de l’auteur. Minecraft, NeoForge et leurs marques appartiennent à leurs titulaires respectifs ; les fichiers Minecraft téléchargés par Gradle ne sont pas redistribués dans ce dépôt.

Les dix-huit sons de la 0.6 sont synthétisés par `scripts/soundscape_audio.py` (oscillateurs, bruit filtré et enveloppes), puis encodés par ffmpeg. Aucun échantillon vocal, morceau de musique ou enregistrement tiers n’est incorporé.

Les seize effets de la 0.9 sont synthétisés par `scripts/awakening_audio.py` avec des oscillateurs, bruit filtré et enveloppes propres, puis encodés par ffmpeg. Les modèles articulés, les textures 16 × 16 et les deux nouvelles architectures sont également des créations originales du projet.
