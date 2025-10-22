package arch;

import jason.asSemantics.Message;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;

import java.util.logging.Level;

public class ProtectorArch extends LocalMinerArch {

    @Override
    public void checkMail() {
        try {
            super.checkMail();

            for (Message m : getTS().getC().getMailBox()) {
                if (m.getPropCont() instanceof Literal content) {
                    if (content.getFunctor().equals("gold") && content.getArity() == 2) {
                        try {
                            int gx = (int) ((NumberTerm) content.getTerm(0)).solve();
                            int gy = (int) ((NumberTerm) content.getTerm(1)).solve();

                            getTS().getAg().addBel(Literal.parseLiteral("gold_reported(" + gx + "," + gy + ")"));
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Failed to parse gold coordinates from message: " + m.getPropCont().toString(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking email in ProtectorArch!", e);
        }
    }
}
