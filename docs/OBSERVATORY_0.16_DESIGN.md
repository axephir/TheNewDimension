# 0.16 — L’Observatoire du Premier Silence

Direction validée : sanctuaire titanesque d’une civilisation ancienne, roche noire métallique, aiguilles monumentales, gravures lumineuses fines, architecture originale inspirée de l’atmosphère de l’Observatoire de Black Flag Resynced. Les images de référence restent externes ; aucune ressource Ubisoft incorporée.

Objectif : première exploration de 3 h 30 à 4 h, sans échec aux boss, pour un personnage et un familier de fin de progression. C’est une cible à éprouver par parcours chronométré, pas une durée garantie. Pas de couloirs artificiellement allongés ni d’attente imposée pour atteindre ce chiffre.

## Contrat de parcours

Six actes, huit grandes chambres par acte, 48 stations sur un itinéraire en serpentin. Emprise réservée 1 280 × 1 024 blocs, six familles architecturales, chemins de 9 blocs de large, sorties visibles. Les chambres disposent de galeries, d’annexes courtes et de plafonds différenciés. Les bandes lumineuses indiquent le prochain passage ; les états des stations distinguent attente, activité et accomplissement. Un terminal donne une carte pratique, des objectifs et un retour vers les étapes déjà atteintes.

Actes : Parvis des Veilleurs ; Archives enfouies ; Forges du Silence ; Jardins pétrifiés ; Tribunal des Jumeaux ; Cœur de l’Observatoire.

## Les six axes autorisés

1. Architecture et récit environnemental : 48 chambres, extérieurs, supports jusqu’au terrain, couronne de pics, éclairage réel, inscriptions brèves et indices facultatifs.
2. Sécurité : relais, témoins de surveillance, rencontres limitées, désactivation locale, mécanismes lisibles, sortie possible.
3. Combats : colosses, gardiens de lentilles, rencontres coordonnées en duo, boss final à phases ; préparation des attaques, fenêtres de riposte, participants et récompenses persistants.
4. Industrie : matériaux et schémas acquis dans l’expédition ; chaîne de raffinage et d’assemblage alimentée en FE et automatisable ; coûts finis, ni matière ni énergie gratuites.
5. Familier : réactions, aide aux caches et à la détection ; toute étape principale possible sans familier disponible ; soin et transport existants conservés.
6. Finition : thème astral, ambiance locale, effets réglables, tests de génération, sauvegarde, progression, protections, boss et machines ; galerie native et publication après validation.

## Contraintes de réalisation

- Vestige est conservé, Nacre reste distincte. Les nouveaux lieux demandent des chunks inexplorés.
- Génération déterministe, bornée au chunk courant, sans chargement des voisins. Marqueur des seuls chunks générés ; aucune construction personnelle ancienne protégée ou remplacée rétroactivement.
- Rencontre perdue ou déchargée ≠ victoire. Reprises réinitialisables ; progression et récompenses ne se dupliquent pas.
- Les boss respectent collisions et visibilité. Les attaques ne détruisent aucun bâtiment. Les familiers apprivoisés ne deviennent pas des cibles des dispositifs.
- Retour et itinéraire principal restent identifiables. Les aides pratiques n’expliquent pas le secret du lieu.
- Les atlas et modèles portés de la 0.15.1 restent intacts.

## Journal de réalisation

- Référence visuelle validée par l’utilisateur le 23 octobre 2026 ; début d’implémentation sur la base 64f296f.
- Architecture technique : région déterministe séparée de Nacre, feature locale, 48 stations persistantes, registre d’expédition serveur, écran cartographique, nouvelles entités et extension industrielle.
- Implémentation : six actes, 48 chambres nommées, six accords couplés, relais actifs, parcours de galeries, 96 réserves, 36 fragments distincts, dix types de gardiens et quatre machines FE.
- Contrôle technique intermédiaire : 175 tests natifs communs réussis, puis 19 tests de l'Observatoire incluant les seuils, les interactions couplées et le butin réel du duo. Contrôle final et captures à consigner dans le rapport distribué.
- La cible de 3 h 30–4 h n'est pas présentée comme une mesure ; une campagne humaine complète et chronométrée reste nécessaire pour la confirmer.
- Validation de livraison : 179 GameTests natifs requis réussis, 53 119 assertions du noyau, 24 groupes de contrôles des ressources et décodage des 65 OGG. Les neuf nouveaux sprites sont distincts, en 16 × 16, avec au plus seize couleurs et alpha binaire. Les empreintes des neuf atlas/modèles d'armures portées et des 58 sons antérieurs sont conservées.
- Vérification dans le client : génération naturelle, carte, accord couplé réellement résolu et validé, combat et couches du boss final, comportement du duo, reprise de sauvegarde, disparition de la barre après voyage, quatre productions industrielles réelles, guide en 854 × 480 et icônes. Inventaire/progression et résistance préparés dans une copie privée ; aucune campagne complète ni durée de parcours mesurée revendiquée.
