import java.util.ArrayList;
import java.util.Scanner;

class Player {

    // si c'est une action null == REST

    // #TODO (plus tard) CAST id times LEARN id

    private static Scanner in;

    static final int REST_ID = 999999;

    static int actionsDispo;
    static ArrayList<Action> actions = new ArrayList<Action>();
    static ArrayList<Action> spellsAvailable = new ArrayList<Action>();
    static ArrayList<Action> spellsLearned = new ArrayList<Action>();
    static ArrayList<Action> spellsInTome = new ArrayList<Action>();
    static ArrayList<Action> opponentSpells = new ArrayList<Action>();
    static ArrayList<Action> potions = new ArrayList<Action>();

    static ArrayList<Integer> inv = new ArrayList<Integer>();
    static int score;

    static ArrayList<Integer> invEnnemy = new ArrayList<Integer>();
    static int scoreEnnemy;

    public static void main(String args[]) {
        in = new Scanner(System.in);

        // game loop
        while (true) {

            initialise();

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            // BREW <id> | CAST <id> [<times>] | LEARN <id> | REST | WAIT

            if (spellsLearned.size() < 8) {
                learn(spellsInTome.get(0));
            } else {
                Action bestAction = new Action();
                Action bestPotion = new Action();
                boolean canMakepotion = false;
                int nbTourForCreatePotion = 99;
                for (Action potion : potions) { // pour chaque potion
                    if (canUse(potion)) { // si elle est possible
                        if (potion.getPrice() > bestAction.getPrice()) { // et qu'elle rapporte plus
                            bestAction = potion; // je la choisie
                            canMakepotion = true;
                        }
                    }
                }
                for (Action potion : potions) { // pour chaque potion
                    if (!canMakepotion) { // si je ne peux pas faire de potion tout de suite
                        ArrayList<Integer> result = getNbToursforMakePotion(potion);
                        if (getAction(result.get(1)) != null) {
                            System.err.println("potion id : " + potion.getActionId());
                            System.err.println("nbTourForCreatePotion : " + result.get(0));
                            if (result.get(0) < nbTourForCreatePotion) { // et que cette potion est plus rapide a faire
                                nbTourForCreatePotion = result.get(0); // je sauvegarde le nombre de tours necessaires pour la faire
                                bestAction = getAction(result.get(1)); // je choisi la premiere action pour concocter cette potion
                            } else if (result.get(0) == nbTourForCreatePotion) { // si la potion met autant de temps poue être concoctée
                                if (potion.getRealPrice() > bestPotion.getRealPrice()) { // et que son prix de vente est plus grand
                                    nbTourForCreatePotion = result.get(0); // je sauvegarde le nombre de tours necessaires pour la faire
                                    bestAction = getAction(result.get(1)); // je choisi la premiere action pour concocter cette potion
                                }
                            }
                        }
                    }
                }
                System.err.println("bestAction : " + bestAction.getActionId());
                play(bestAction);
            }
        }
    }

    public static void initialise() {
        actions = new ArrayList<Action>();
        spellsAvailable = new ArrayList<Action>();
        spellsLearned = new ArrayList<Action>();
        spellsInTome = new ArrayList<Action>();
        opponentSpells = new ArrayList<Action>();
        potions = new ArrayList<Action>();

        inv = new ArrayList<Integer>();
        invEnnemy = new ArrayList<Integer>();

        actionsDispo = in.nextInt(); // le nombre d'actions possibles
        for (int i = 0; i < actionsDispo; i++) {
            Action action = new Action();
            action.setActionId(in.nextInt()); // the unique ID of this spell or recipe
            action.setActionType(in.next()); // CAST, OPPONENT_CAST, LEARN, BREW

            ArrayList<Integer> ressources = new ArrayList<Integer>();
            for (int j = 0; j < 4; j++) {
                ressources.add(in.nextInt()); // BREW : le cout SPELL : l'apport ou le cout
            }
            action.setRessources(ressources); // si potion : les ressources necessaires, si spell : les ressources qu'il utilise et apporte

            action.setPrice(in.nextInt()); // the price in rupees if this is a potion
            action.setTomeIndex(in.nextInt()); // in the first two leagues: always 0; later: the index in the tome if
                                               // this is a tome spell, equal to the read-ahead tax; For brews, this is
                                               // the value of the current urgency bonus
            action.setTaxCount(in.nextInt()); // in the first two leagues: always 0; later: the amount of taxed tier-0
                                              // ingredients you gain from learning this spell; For brews, this is how
                                              // many times you can still gain an urgency bonus
            action.setCastable(in.nextInt() != 0); // 1 if this is a castable player spell else 0
            action.setRepeatable(in.nextInt() != 0); // for the first two leagues: always 0; later: 1 if this is a
                                                     // repeatable player spell
            actions.add(action);
            switch (action.getActionType()) {
            case "CAST":
                if (action.getTomeIndex() == -1) {
                    spellsLearned.add(action);
                    if (action.isCastable()) {
                        spellsAvailable.add(action);
                    }
                }
                break;
            case "BREW":
                potions.add(action);
                break;
            case "OPPONENT_CAST":
                opponentSpells.add(action);
                break;
            case "LEARN":
                if (action.getTomeIndex() >= 0) {
                    spellsInTome.add(action);
                }
                break;
            }
        }

        for (int j = 0; j < 4; j++) {
            inv.add(in.nextInt());
        }
        score = in.nextInt(); // amount of rupees

        for (int j = 0; j < 4; j++) {
            invEnnemy.add(in.nextInt());
        }
        scoreEnnemy = in.nextInt(); // amount of rupees
    }

    /**
     * @param potion
     * @return une ArrayList<Integer> :
     * le premier element correspond au nombre de tours nécéssaires pour faire la potion
     * le deuxieme element correspond a l'id du premier spell a utiliser pour concocter cette potion
     */
    public static ArrayList<Integer> getNbToursforMakePotion(Action potion) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int nbTours = 1;
        result.add(nbTours);

        int elementsManquants = 0;
        for (int i = 0; i < inv.size(); i++) { // pour chaque type d'element
            elementsManquants = (inv.get(i) + potion.getRessources().get(i)) * (-1);
            if (elementsManquants > 0) { // si il m'en manque
                ArrayList<Integer> infoElement = infoGetElementType(i, elementsManquants);
                if (infoElement != null) {
                    result.set(0, nbTours + infoElement.get(0)); // j'ajoute le nombre de tours necessaires pour les obtenirs
                    if (result.size() < 2) {
                        result.add(infoElement.get(1)); // j'ajoute l'id du prochain sort a jouer
                    } else {
                        result.set(1, infoElement.get(1)); // je modifie l'id du prochain sort a jouer
                    }
                } else {
                    result.set(0, result.get(0) + 2);
                    if (result.size() < 2) {
                        Action actionToHave2OfAll = getSpellToHave2OfAll();
                        if (actionToHave2OfAll != null) {
                            result.add(actionToHave2OfAll.getActionId());
                        } else {
                            result.add(REST_ID);
                        }
                    } else {
                        if (result.get(1) == REST_ID) {
                            Action actionToHave2OfAll = getSpellToHave2OfAll();
                            if (actionToHave2OfAll != null) {
                                result.set(1, actionToHave2OfAll.getActionId());
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param elementTypeId   l'id du type d'element que l'on souhaite obtenir
     * @param nbElementsObjectif le nombre d'element de ce type que l'on souhaite obtenir
     * @return ArrayList<Integer> result :
     * le premier element correspond au nombre de tours nécessaires pour obtenir le nombre d'element que l'on souhaite obtenir
     * (prendre en compte les sorts repetables mais plus tard)
     * le deuxieme élément correspond a l'id du prochain sort à utiliser
     */
    public static ArrayList<Integer> infoGetElementType(int elementTypeId, int nbElementsObjectif) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        ArrayList<Integer> _inv = new ArrayList<Integer>();
        for (Integer i : inv) {
            _inv.add(i);
        }

        int nbElements = 0;
        int nbToursRequired = 1;

        Action bestSpell = null;
        ArrayList<Action> _spellsAvailable = spellsAvailable;

        if (_spellsAvailable.isEmpty()) { // s'il n'y a aucun sort disponible
            result.add(REST_ID); // on l'ajoute a la liste l'action de se reposer
            nbToursRequired++;
            _spellsAvailable = spellsLearned;
        }

        for (Action spell : _spellsAvailable) { // pour chaque sort disponible
            if (canUse(spell, _inv)) { // si je peux l'utiliser
                int nb = spell.getRessources().get(elementTypeId); // on stock le nombre de ressource qu'il rapporte
                if (nb > 0) { // s'il en rapporte
                    try {
                        if (nb > bestSpell.getRessources().get(elementTypeId)) { // si ce sort rapporte plus que le sort choisi
                            bestSpell = spell; // on le choisi
                            nbElements = nb;
                        }
                    } catch (Exception e) { // si on a pas encore choisi de sort
                        bestSpell = spell;
                        nbElements = nb;
                    }
                }
            }
            if (nbElements >= nbElementsObjectif) { // si l'objectif a été atteint
                result.add(nbToursRequired);
                result.add(bestSpell.getActionId());
                return result;
            }
        }

        result.add(nbToursRequired);
        if (bestSpell != null) { // si on a choisi un sort
            result.add(bestSpell.getActionId()); // on l'ajoute a la liste
            _spellsAvailable.remove(getAction(bestSpell.getActionId())); // on supprime le sort choisi des sorts disponibles
            updateInvAfterSpell(bestSpell, _inv); // on met a jour notre inventaire (une copie pour check ce cas uniquement)
        } else {
            result.add(REST_ID); // sinon on se repose
            nbToursRequired++;
            result.set(0, nbToursRequired);
            _spellsAvailable = spellsLearned;
        }

        int nbElementsWithAdd = nbElements;
        while (nbElements < nbElementsObjectif && nbToursRequired < 10) {
            Action bestSpellSuite = null;

            if (_spellsAvailable.isEmpty()) {
                nbToursRequired++;
                result.set(0, nbToursRequired);
                _spellsAvailable = spellsLearned;
            }
            for (Action spell : _spellsAvailable) { // pour chaque sort disponible
                if (canUse(spell, _inv)) { // si je peux l'utiliser
                    int nb = spell.getRessources().get(elementTypeId); // on stock le nombre de ressource qu'il rapporte
                    if (nb > 0) { // s'il en rapporte
                        try {
                            if (nb > bestSpellSuite.getRessources().get(elementTypeId)) { // si ce sort rapporte plus que le sort choisi
                                bestSpellSuite = spell; // on le choisi
                                nbElementsWithAdd = nbElements + nb; // on met a jour le nombre d'elements qu'on recupere
                            }
                        } catch (Exception e) { // si on a pas encore choisi de sort
                            bestSpellSuite = spell;
                            nbElementsWithAdd += nb;
                            nbToursRequired++;
                            result.set(0, nbToursRequired);
                        }
                    }
                }
                if (nbElementsWithAdd >= nbElementsObjectif) { // si l'objectif a été atteint
                    result.add(bestSpellSuite.getActionId());
                    return result;
                }
            }
            nbElements = nbElementsWithAdd;

            if (bestSpellSuite != null) { // si on a choisi un sort
                result.add(bestSpellSuite.getActionId()); // on l'ajoute a la liste
                _spellsAvailable.remove(getAction(bestSpellSuite.getActionId())); // on supprime le sort choisi des sorts disponibles
                updateInvAfterSpell(bestSpell, _inv); // on met a jour notre inventaire (une copie pour check ce cas uniquement)
            } else { // sinon on se repose
                result.add(REST_ID); // on l'ajoute a la liste
                nbToursRequired++;
                result.set(0, nbToursRequired);
                _spellsAvailable = spellsLearned;
            }
        }

        return null;
    }

    /**
     * @return un sort qui me fait recuperer un type de ressource que je possede moins de 2 fois
     */
    public static Action getSpellToHave2OfAll() {
        for (Action action : spellsAvailable) { // pour chaque sort dispo
            if (canUse(action)) {
                for (int i = 0; i < inv.size(); i++) { // pour chaque type d'element
                    if (inv.get(i) < 2) { // si j'en ai moins que 2
                        if (action.getRessources().get(i) > 0) { // et que ce sort m'en rapporte
                            return action;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * determine si l'action peut être utilisé ce tour la
     * @param action
     * @return
     */
    public static boolean canUse(Action action) {
        switch (action.getActionType()) {
        case "CAST":
            if (action.isCastable()) { // si le sort n'a pas déja été utilisé
                boolean ok = getListeSum(inv) + getListeSum(action.getRessources()) < 10;
                for (int i = 0; i < inv.size(); i++) {
                    //                    System.err.println("inv.get(" + i + ") :" + inv.get(i));
                    //                    System.err.println("action.getRessources().get(" + i + ") :" + action.getRessources().get(i));
                    //                    System.err.println("(inv.get(i) + action.getRessources().get(i)) : " + (inv.get(i) + action.getRessources().get(i)));
                    if ((inv.get(i) + action.getRessources().get(i)) < 0 || !ok) { // si je n'ai pas asser de ressource pour l'utiliser
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        case "BREW":
            for (int i = 0; i < inv.size(); i++) {
                if (inv.get(i) < (action.getRessources().get(i) * -1)) {
                    return false;
                }
            }
            return true;
        default:
            return false;
        }
    }

    /**
     * determine si l'action peut être utilisé ce tour la
     * @param action
     * @return
     */
    public static boolean canUse(Action action, ArrayList<Integer> _inv) {
        switch (action.getActionType()) {
        case "CAST":
            if (action.isCastable()) { // si le sort n'a pas déja été utilisé
                boolean ok = getListeSum(inv) + getListeSum(action.getRessources()) < 10;
                for (int i = 0; i < inv.size(); i++) {
                    //                    System.err.println("inv.get(" + i + ") :" + inv.get(i));
                    //                    System.err.println("action.getRessources().get(" + i + ") :" + action.getRessources().get(i));
                    //                    System.err.println("(inv.get(i) + action.getRessources().get(i)) : " + (inv.get(i) + action.getRessources().get(i)));
                    if ((inv.get(i) + action.getRessources().get(i)) < 0 || !ok) { // si je n'ai pas asser de ressource pour l'utiliser
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        case "BREW":
            for (int i = 0; i < inv.size(); i++) {
                if (inv.get(i) < (action.getRessources().get(i) * -1)) {
                    return false;
                }
            }
            return true;
        default:
            return false;
        }
    }

    /**
     * effectue la mise a jour des éléments dans l'inventaire a l'utilisation d'un sort
     * @param spell le sort utilisé
     * @param _inv un inventaire
     * @return true ssi le sort peut être utilisé
     */
    public static boolean updateInvAfterSpell(Action spell, ArrayList<Integer> _inv) {
        int totalElementsInInv = getListeSum(_inv);
        boolean pb = false;
        try {
            for (int i = 0; i < _inv.size() && !pb; i++) { // pour chaque type d'element, tant qu'il n'y a pas de probleme
                int result = _inv.get(i) + spell.getRessources().get(i);
                if (result >= 0 && totalElementsInInv + spell.getRessources().get(i) <= 10) { // si l'inventaire a les ressources et la place nécéssaire 
                    totalElementsInInv += spell.getRessources().get(i); // on met a jour le nombre total d'éléments dans l'inventaire
                    _inv.set(i, result); // on met a jour l'inventaire
                } else {
                    pb = true;
                }
            }
            return pb;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param liste
     * @return la somme des elements d'une liste d'entiers
     */
    public static int getListeSum(ArrayList<Integer> liste) {
        int resultat = 0;
        for (Integer integer : liste) {
            resultat += integer;
        }
        return resultat;
    }

    /**
     * @param actionId
     * @return l'action qui correspond
     */
    public static Action getAction(int actionId) {
        for (Action action : actions) {
            if (action.getActionId() == actionId) {
                return action;
            }
        }
        return null;

    }

    // BREW id: votre sorcière tente de préparer la potion avec l'identifiant donné.
    // CAST id: votre sorcière tente de lancer le sort avec l'identifiant donné.
    // REST: votre sorcière récupère, vos sorts épuisés redeviennent lansables.
    // WAIT: votre sorcière ne fait rien.

    public static void play(Action action) {
        try {
            System.err.println("actionType : " + action.getActionType());
            switch (action.getActionType()) {
            case "CAST":
                cast(action.getActionId());
                break;
            case "BREW":
                brew(action.getActionId());
                break;
            default:
                rest();
                break;
            }
        } catch (Exception e) {
            rest();
        }
    }

    public static void brew(int id) {
        System.out.println("BREW " + id + " je prepare la potion avec l'id :" + id);
    }

    public static void brew(int id, String message) {
        System.out.println("BREW " + id + " " + message);
    }

    public static void cast(int id) {
        if (id == REST_ID) {
            rest();
            return;
        }
        System.out.println("CAST " + id + " je lance le sort d'id : " + id);
    }

    public static void cast(int id, String message) {
        if (id == REST_ID) {
            rest();
            return;
        }
        System.out.println("CAST " + id + " " + message);
    }

    public static void learn(Action action) {
        System.out.println("LEARN " + action.getActionId() + " j'apprend le sort d'id : " + action.getActionId());
    }

    public static void rest() {
        System.out.println("REST je me repose");
    }

    public static void rest(String message) {
        System.out.println("REST " + message);
    }
}