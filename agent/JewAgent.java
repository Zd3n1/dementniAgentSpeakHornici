package agent;

import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Unifier;
import jason.asSyntax.Trigger;

import java.util.Iterator;
import java.util.Queue;

/**
 * Upravuje výchozí funkci pro výběr událostí s cílem upřednostnit některé události před běžnými.
 *
 * Konkrétně zajišťuje, že vnímané zlato (+cell(_,_,gold)) a signál pro restart simulace (+restart) jsou zpracovány
 * okamžitě, před jakoukoli jinou událostí (např. přijatými zprávami, selháním interních cílů nebo vnímáním pohybu).
 * Tato strategie pomáhá agentovi rychle reagovat na přítomnost zlata a zajišťuje promptní reset na začátku
 * nového běhu simulace. Agent se tedy za každou cenu snaží sbírat zlato a rychle reagovat na restart.
 * Takže to bude pěknej žiďák a skrblík voe.
 */
public class JewAgent extends Agent {

    private Trigger gold = Trigger.parseTrigger("+cell(_,_,gold)");
    private Trigger restart = Trigger.parseTrigger("+restart");
    private Unifier un = new Unifier();

    public Event selectEvent(Queue<Event> events) {
        Iterator<Event> ie = events.iterator();
        while (ie.hasNext()) {
            un.clear();
            Event e = ie.next();
            if (un.unifies(gold, e.getTrigger()) || un.unifies(restart, e.getTrigger())) {
                ie.remove();
                return e;
            }
        }
        return super.selectEvent(events);
    }
}
