"""Reproducible vault state models, rewards and bilingual gameplay instructions."""
def content(write,texture,assets,data):
    for name in ['vault_core','vault_seal']:
        variants={}
        for facing,rotation in [('north',0),('east',90),('south',180),('west',270)]:
            for seal in range(3):
                for lit in [False,True]:
                    model=name+('_active' if lit else '')
                    variants[f'facing={facing},seal={seal},lit={str(lit).lower()}']={'model':'tnd:block/'+model,'y':rotation}
                    variants[f'facing={facing},seal={seal},lit={str(lit).lower()},mirrored=true']=variants.pop(f'facing={facing},seal={seal},lit={str(lit).lower()}')
                    variants[f'facing={facing},seal={seal},lit={str(lit).lower()},mirrored=false']=dict(variants[f'facing={facing},seal={seal},lit={str(lit).lower()},mirrored=true'])
        write(assets/f'blockstates/{name}.json',{'variants':variants})
        for lit in [False,True]:
            model=name+('_active' if lit else '')
            if lit:texture(model)
            write(assets/f'models/block/{model}.json',{'parent':'minecraft:block/cube',
                'textures':{'particle':'tnd:block/'+model,'down':'tnd:block/cut_alloy','up':'tnd:block/'+model,
                'north':'tnd:block/'+model,'south':'tnd:block/ancient_alloy','west':'tnd:block/riveted_alloy','east':'tnd:block/riveted_alloy'}})
    write(data/'recipe/resonant_aegis.json',{'type':'minecraft:crafting_shapeless',
        'ingredients':['tnd:resonance_heart','tnd:composite_crystal','tnd:composite_crystal','minecraft:iron_ingot','minecraft:iron_ingot'],
        'result':{'id':'tnd:resonant_aegis','count':1}})

def languages(fr,en):
    fr.update({
        'block.tnd.vault_core':'Noyau du sanctuaire','block.tnd.vault_seal':'Sceau de résonance','block.tnd.mural_vault':'Fresque des trois épreuves',
        'item.tnd.resonance_heart':'Cœur de résonance','item.tnd.resonant_aegis':'Égide résonante',
        'tooltip.tnd.resonance_heart':'Trésor partagé d’un sanctuaire sécurisé. Sert à fabriquer l’égide résonante.',
        'tooltip.tnd.resonant_aegis':'1 poussière : deux cœurs d’absorption pendant 15 s. Recharge : 60 s. Ne remplace pas une absorption existante.',
        'message.tnd.vault_unbound':'Ce mécanisme est détaché de son sanctuaire. Seuls les sites anciens contiennent une épreuve.',
        'message.tnd.vault_seal_done':'Cette aile est déjà sécurisée.',
        'message.tnd.vault_busy':'Une épreuve est en cours. Vainquez ses gardiens ou quittez le sanctuaire pour la réinitialiser.',
        'message.tnd.vault_order':'Activez d’abord le sceau %s : bleu, vert, puis rouge.',
        'message.tnd.vault_peaceful':'Les épreuves nécessitent une difficulté autre que Paisible. Aucun combustible consommé.',
        'message.tnd.vault_fuel':'Insérez une poussière d’écho dans votre inventaire pour alimenter le sceau.',
        'message.tnd.vault_blocked':'La zone d’apparition est obstruée ou déchargée. Dégagez-la ; aucune poussière consommée.',
        'message.tnd.vault_start':'Épreuve %s : %s gardien(s). Esquivez les cercles ou interrompez les charges.',
        'message.tnd.vault_clear':'Aile sécurisée : %s / 3. Le noyau central conserve votre progression.',
        'message.tnd.vault_progress':'Ailes sécurisées : %s / 3. Activez les sceaux dans l’ordre : bleu, vert, rouge.',
        'message.tnd.vault_empty':'Le trésor commun de ce sanctuaire a déjà été récupéré.',
        'message.tnd.vault_reward':'Sanctuaire sécurisé : cœur de résonance, quatre fragments et huit poussières. Partagez ce trésor avec votre groupe.',
        'message.tnd.vault_bar':'Épreuve %s — %s gardien(s) restant(s)',
        'message.tnd.aegis_existing':'Une absorption vous protège déjà. L’égide conserve sa poussière.',
        'message.tnd.aegis_active':'Égide activée : deux cœurs temporaires pendant quinze secondes.',
        'message.tnd.lore.vault':'Trois couplages : bleu, vert, puis rouge. Une poussière d’écho éveille chaque sceau. Leurs gardiens tracent l’impact avant de frapper : sortez du cercle ou brisez la charge. Les issues restent libres. Après les trois épreuves, réclamez le cœur dans la nef. Ne confondez pas le silence avec un arrêt définitif.'})
    en.update({
        'block.tnd.vault_core':'Sanctuary Core','block.tnd.vault_seal':'Resonance Seal','block.tnd.mural_vault':'Mural of Three Trials',
        'item.tnd.resonance_heart':'Resonance Heart','item.tnd.resonant_aegis':'Resonant Aegis',
        'tooltip.tnd.resonance_heart':'Shared treasure from a secured sanctuary. Crafts the Resonant Aegis.',
        'tooltip.tnd.resonant_aegis':'1 dust: two absorption hearts for 15 s. Cooldown: 60 s. Preserves existing absorption.',
        'message.tnd.vault_unbound':'This mechanism is detached from its sanctuary. Only ancient sites contain a trial.',
        'message.tnd.vault_seal_done':'This wing is already secured.',
        'message.tnd.vault_busy':'A trial is active. Defeat its guardians or leave the sanctuary to reset it.',
        'message.tnd.vault_order':'Activate seal %s first: blue, green, then red.',
        'message.tnd.vault_peaceful':'Trials require a difficulty above Peaceful. No fuel consumed.',
        'message.tnd.vault_fuel':'Keep one echo dust in your inventory to power the seal.',
        'message.tnd.vault_blocked':'The spawn area is obstructed or unloaded. Clear it; no dust consumed.',
        'message.tnd.vault_start':'Trial %s: %s guardian(s). Evade the circles or interrupt their charges.',
        'message.tnd.vault_clear':'Wing secured: %s / 3. The central core preserves your progress.',
        'message.tnd.vault_progress':'Secured wings: %s / 3. Activate the blue, green, then red seals.',
        'message.tnd.vault_empty':'The shared treasure of this sanctuary has already been claimed.',
        'message.tnd.vault_reward':'Sanctuary secured: resonance heart, four fragments and eight dust. Share the treasure with your party.',
        'message.tnd.vault_bar':'Trial %s — %s guardian(s) remaining',
        'message.tnd.aegis_existing':'Absorption already protects you. The aegis saves its dust.',
        'message.tnd.aegis_active':'Aegis active: two temporary hearts for fifteen seconds.',
        'message.tnd.lore.vault':'Three couplings: blue, green, then red. One echo dust wakes each seal. Guardians mark their impact before striking: leave the circle or break their charge. Exits remain open. After the three trials, claim the heart in the nave. Do not mistake silence for a permanent shutdown.'})
