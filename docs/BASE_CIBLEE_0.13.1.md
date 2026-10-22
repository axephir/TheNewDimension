# 0.13.1 — Une empreinte personnelle

Minecraft Java 26.2 · NeoForge 26.2.0.93 · Java 25. Remplacer l’ancien JAR TND côté client et serveur.

La scène de convergence privilégie désormais les installations du joueur plutôt que le seul emplacement du réacteur. La caméra cadre ce qui a été construit et utilisé, en se rapprochant si un toit ou un relief masque la vue ; les impacts se produisent pendant le plan et visent les réserves et les équipements. Le noyau reste accessible dans la scène personnelle, même lorsque le réacteur est éloigné. Passer la cinématique ou désactiver ses animations conserve le même état final des ruines.

<details>
<summary>Détection de la base, fonctionnement et compatibilité — indications sur la conclusion</summary>

## Reconnaître une installation personnelle

Le serveur enregistre le véritable auteur de chaque placement de coffre, baril, four, machine ou autre bloc doté d’un inventaire, d’énergie ou de stockage de fluide. Cette propriété est enregistrée sur le bloc ; un index personnel borné à 4 096 positions permet de retrouver une base non chargée. Les placements annulés et les faux joueurs d’automatisation ne sont pas enregistrés. La propriété et l’index sont sauvegardés ; l’index survit à la mort.

Au lancement, le système cherche un lit correspondant au point de réapparition personnel en Overworld. Le spawn global du monde et les points imposés par commande ne le remplacent pas. Le lit doit encore exister. Dans un rayon horizontal de 64 blocs et une différence de hauteur de 48 blocs, il faut **au moins dix équipements dont le joueur est le véritable auteur**. Ils doivent former un groupe compact : au plus 24 blocs de décalage horizontal et 12 de hauteur autour d’un même équipement. La recherche franchit les frontières des chunks ; les deux moitiés placées d’un double coffre comptent comme deux blocs.

Les positions sont vérifiées sur les blocs actuels : un coffre cassé, remplacé ou repris par un autre joueur ne conserve pas son ancienne attribution dans la recherche. Les coffres générés, l’ouverture d’un coffre et les installations des autres joueurs ne constituent pas des preuves. Parmi les regroupements admissibles, les conteneurs occupés ont davantage de poids que les conteneurs vides. La recherche ne fouille pas les inventaires pour établir une valeur marchande et ne génère pas les tables de butin.

L’analyse examine un seul chunk indexé par tick, au maximum 81, avec un accès initial au chunk du lit. La copie conserve ses limites de taille configurées ; les équipements hors de cette empreinte ne sont pas retenus. Un petit rayon de projection peut donc empêcher la sélection d’une installation trop éloignée du lit. Le groupe retenu donne jusqu’à huit points d’impact espacés, sur les équipements eux-mêmes. Le stockage reste visible avant les impacts ; une partie des débris supérieurs disparaît pendant le plan pour éviter un toit entièrement intact au-dessus d’un cratère.

## Repli et anciennes parties

Sans lit valide et regroupement prouvé, la scène reste centrée sur l’installation du réacteur. Elle n’utilise pas le stockage d’un village ou celui d’un voisin pour deviner une base.

Minecraft ne conservait pas l’auteur des coffres déjà présents. La mise à jour ne peut pas reconstituer cette information de façon fiable : **les anciens conteneurs non attribués ne comptent qu’après avoir été cassés puis replacés par le joueur**. Vider les coffres avant de les déplacer reste la manière pratique de réorganiser son stockage. Aucun clic de consultation ne revendique un ancien coffre. Les constructions déjà sauvegardées ne sont pas déplacées ni régénérées par cette version.

## Une scène isolée

Les impacts modifient exclusivement une copie personnelle dans Rémanence. Les inventaires de coffres ne sont pas copiés ; les machines apparaissent comme des épaves inertes sans énergie. La base réelle, le lit, les ressources et les modifications des autres joueurs restent dans le monde source. Le retour vise la position originale près du réacteur, indépendamment du lieu choisi pour la scène.

Le signal de la scène est sauvegardé séparément de la position de retour. La récupération après interruption garde les journaux vérifiés et les garde-fous de la 0.13. `/tnd recover` conserve sa fonction.

</details>

## Validation

Six scénarios natifs supplémentaires vérifient le placement réel en Survie, les sauvegardes de propriétaire, les regroupements à cheval sur des chunks, le seuil de dix, les équipements étrangers et générés, le lit manquant, une base à 512 blocs du réacteur, les impacts sur son stockage, le retour, une déconnexion en cours de scène et le passage de la cinématique. La suite complète compte désormais **150 scénarios Minecraft** ; le compte rendu publié avec le JAR indique les résultats effectifs.
