package agent;

import jason.asSemantics.Agent;

public class ProtectorAgent extends Agent {

    /**
     * Přepisujeme metodu pro načtení ASL kódu.
     * Místo načítání ASL podle agentova jména (konvence),
     * načteme explicitně soubor 'protector.asl'.
     */
    @Override
    public void loadAS(String asSrc) throws Exception {
        String protectorFileName = "protector.asl";

        // Pro jistotu zkontrolujeme, zda soubor existuje v cesta/asl/
        // Pokud je agent spouštěn ze složky projektu, cesta bude:
        String fullPath = "asl/" + protectorFileName;

        try {
            super.loadAS(fullPath); // Volá původní metodu s explicitním jménem souboru
        } catch (Exception e) {
            System.out.println("Failed to load asl file: " + fullPath + ". There might be a syntax error in it.");
        }
    }
}
