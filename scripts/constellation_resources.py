"""Bilingual text for the client constellation atlas; progression IDs stay unchanged."""
def languages(fr,en):
 pairs={
  'title':('Constellations','Constellations'),
  'talents':('Talents','Talents'),'talents_selected':('• Talents','• Talents'),
  'titles':('Titres','Titles'),'titles_selected':('• Titres','• Titles'),
  'close':('Fermer · Échap','Close · Escape'),
  'level':('niv. %s','lv. %s'),'points':('%s points','%s points'),
  'rank':('Rang %s / %s','Rank %s / %s'),'cost':('Coût en points : %s','Point cost: %s'),
  'type.foundation':('Fondation','Foundation'),'type.passive':('Spécialisation passive','Passive specialization'),'type.active':('Capacité active','Active ability'),
  'state.complete':('Acquis · rang maximal','Learned · maximum rank'),'state.available':('Prochain rang accessible','Next rank available'),'state.locked':('Conditions encore incomplètes','Requirements not yet met'),
  'learn':('Apprendre · %s pts','Learn · %s pts'),'improve':('Améliorer · %s pts','Improve · %s pts'),
  'complete':('Rang maximal','Maximum rank'),'pending':('En cours…','Learning…'),'activate':('Utiliser','Activate'),
  'survival':('Mode Survie requis','Survival mode required'),
  'equip':('Porter ce titre','Equip this title'),'equipped':('Titre porté','Title equipped'),
  'cosmetic':('Distinction cosmétique','Cosmetic distinction'),
  'prestige_level':('Palier : niveau %s','Milestone: level %s'),'current_level':('Votre niveau : %s','Your level: %s'),
  'cooldown_help':('Recharge commune aux capacités : 60 secondes. Utilisation en Survie.','Abilities share a 60-second cooldown. Use in Survival.'),
  'xp':('%s XP au total\n%s XP avant le prochain niveau','%s total XP\n%s XP until the next level'),
  'controls':('Glisser : déplacer · Molette : zoom','Drag to move · Scroll to zoom'),
  'zoom_out':('Dézoomer · touche −','Zoom out · − key'),'zoom_in':('Zoomer · touche +','Zoom in · + key'),
  'fit':('Voir tout l’arbre · touche Début\nPage préc./suiv. : changer de maîtrise\nTab et flèches : parcourir les capacités','Fit the whole tree · Home key\nPage Up/Down: change mastery\nTab and arrows: explore abilities'),
  'motion_on':('Animations discrètes activées\nCliquer pour réduire les mouvements','Subtle animations enabled\nClick to reduce motion'),
  'motion_off':('Mouvements réduits\nCliquer pour réactiver les animations','Reduced motion\nClick to enable animations'),
 }
 for key,(f,e) in pairs.items():fr['tree.tnd.'+key]=f;en['tree.tnd.'+key]=e
