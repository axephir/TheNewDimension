# Développement cloud

Dossier de travail : `/workspace/TheNewDimension`. Réutiliser ce checkout ; aucun worktree supplémentaire n’est nécessaire.

```sh
bash scripts/setup-cloud.sh
python3 scripts/cloud_gradle.py runGameTestServer
python3 scripts/cloud_gradle.py runClient
```

`setup-cloud.sh` réutilise ou installe le JDK Temurin 25.0.4.1+1 sous `/workspace/.toolchains`, vérifie son SHA-256, puis exécute les contrôles des ressources, du domaine et le build. Une autre installation Java 25 peut être indiquée avec `TND_JDK_HOME`. `cloud_gradle.py` transmet le proxy de la session et ajoute son certificat aux racines de confiance locales, sans désactiver TLS. Gradle 9.2.1 et NeoForge 26.2.0.93 sont épinglés dans le projet.

Les téléchargements utilisent les domaines de GitHub et de ses assets, `services.gradle.org`, `downloads.gradle.org`, `repo.maven.apache.org`, `maven.neoforged.net`, `libraries.minecraft.net`, `piston-meta.mojang.com`, `piston-data.mojang.com` et `resources.download.minecraft.net`. Conserver les réglages réseau et les identifiants injectés par l’environnement ; aucun jeton personnel n’est nécessaire au build. Les certificats, les caches, les mondes et les identifiants restent hors du paquet publié.

Les processus graphiques et les serveurs ne survivent pas à une restauration d’environnement : les relancer avec les commandes ci-dessus. Les GameTests utilisent leur monde jetable dans `run-gametest/`, séparé des sauvegardes client et serveur. Un client nécessite un affichage OpenGL ; le build et les GameTests fonctionnent sans affichage. En cloud, le client peut être contrôlé avec un serveur X et un rendu Mesa logiciel.

Publication : `python3 scripts/package_release.py` emballe le JAR déjà construit et les sources dans `/workspace/shared`, contrôle les métadonnées, les PNG et le ZIP, puis écrit les sommes SHA-256. La branche GitHub `downloads` héberge les binaires ; les fichiers publiés doivent être retéléchargés et comparés aux originaux avant d’annoncer leur disponibilité.

Pour un diagnostic ciblé : `python3 scripts/cloud_gradle.py runGameTestServer '-PgameTests=tnd:citadel_layout'`. Le sélecteur accepte les identifiants natifs et les jokers. La vérification de publication doit ensuite exécuter la suite complète sans ce paramètre. Le guide courant est [AWAKENING_0.9.0.md](AWAKENING_0.9.0.md).
