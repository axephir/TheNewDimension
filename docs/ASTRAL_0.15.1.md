# 0.15.1 — Interfaces astrales

La 0.15.1 prolonge la direction artistique de l’arbre de compétences dans les autres interfaces de The New Dimension. Elle refait également les icônes d’inventaire des quatre ensembles d’armure.

## Armures dans l’inventaire

Les seize sprites sont redessinés en **16 × 16 pixels**, avec transparence franche et palette limitée. Casque, plastron, jambières et bottes restent reconnaissables à la taille native de l’inventaire.

| Ensemble | Identité visuelle |
|---|---|
| Umbra | Plaques sombres, éclats violets et sertissages dorés |
| Rempart (Ward) | Métal bronze, angles épais et cœur ambré |
| Traque (Shade) | Silhouette fine, lames bleues et reflets violets |
| Suture (Choir) | Métal argenté, cannelures cyan et lumière froide |

La modification porte sur les objets affichés dans l’inventaire, les emplacements d’équipement et les butins. **Les modèles équipés, leurs atlas et leurs animations ne sont pas modifiés.** Les propriétés, enchantements, durabilités, recettes et bonus de set sont conservés.

## Une même famille d’interfaces

Les treize écrans du mod partagent désormais les surfaces de Constellations : maîtrises, objectifs, archives, dossier du familier, guide de terrain, guide du portail, manuel industriel, extracteur, forge et processeurs apparentés, machines industrielles, moniteur, ancre spatiale et terminaux narratifs.

- Panneaux bleu nuit, bordures fines et touches cyan et or.
- Fond spatial discret ; les cartes de lecture restent opaques.
- Boutons avec états de survol, sélection, focus clavier et indisponibilité distincts.
- Onglets actifs visibles dans les guides, le dossier du familier et les terminaux.
- Emplacements d’objets sombres et contrastés ; coordonnées natives conservées.
- Titres longs abrégés proprement, avec texte intégral au survol.
- Clics discrets sur le canal d’interface, sans son répété au simple survol.
- Scintillement lent et jauges interpolées ; l’option de mouvements réduits supprime ces animations.

Les cinq raccourcis restent **à gauche de l’inventaire**. Le bouton Familier des paramètres adopte aussi ce thème. Les écrans de Minecraft et des autres mods gardent leur présentation habituelle.

Les guides conservent leurs schémas, leurs fiches et leur pagination. Les textes narratifs restent inchangés pour préserver la découverte. La refonte ne change ni les opérations des machines, ni leurs coûts, ni les échanges avec le serveur.

## Installation

Minecraft Java **26.2**, NeoForge **26.2.0.93**, Java **25**. Retirer l’ancien JAR TND et installer la même version côté client et serveur. Cette mise à jour visuelle s’applique aux parties existantes : aucun nouveau chunk n’est nécessaire pour voir les icônes ou les interfaces.

## Vérification

Compilation, contrôles de ressources et assertions du noyau accompagnent la validation du commit. Les GameTests natifs couvrent les systèmes de jeu existants ; l’examen visuel et les interactions s’effectuent également dans le client Minecraft. La galerie de distribution et le fichier de validation détaillent les contrôles réellement effectués et leurs limites.

Les icônes restent reproductibles via `scripts/armor_icons.py`, appelé par les générateurs de ressources. Les textures portées font l’objet d’une comparaison d’empreintes avec la version précédente.
