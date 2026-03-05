package org.mage.test.cards.single.blc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests for Zinnia, Valley's Voice
 *  - Flying
 *  - Gets +X/+0 where X is number of other creatures you control with base power 1
 *  - Creature spells you cast have offspring {2}
 */
public class ZinniaValleysVoiceTest extends CardTestPlayerBase {

    /**
     * Zinnia should get +1/+0 from a single other creature
     * you control with base power 1.
     * Setup:
     *  - Battlefield A: Zinnia, Valley's Voice
     *  - Battlefield A: Llanowar Elves (1/1, base power 1)
     * Expect:
     *  - Zinnia is 2/3 (1 base power + 1 from the Elf)
     */
    @Test
    public void testPowerBonusOneBasePowerOneCreature() {
        addCard(Zone.BATTLEFIELD, playerA, "Zinnia, Valley's Voice", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Llanowar Elves", 1);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, "Zinnia, Valley's Voice", 2, 3);
    }

    /**
     * Zinnia should get +2/+0 from two other base-power-1 creatures.
     * Setup:
     *  - Battlefield A: Zinnia, Valley's Voice
     *  - Battlefield A: Llanowar Elves x2
     * Expect:
     *  - Zinnia is 3/3
     */
    @Test
    public void testPowerBonusTwoBasePowerOneCreatures() {
        addCard(Zone.BATTLEFIELD, playerA, "Zinnia, Valley's Voice", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Llanowar Elves", 2);

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPowerToughness(playerA, "Zinnia, Valley's Voice", 3, 3);
    }

    /**
     * Creature spells you cast have offspring {2}.
     * Setup:
     *  - Battlefield A: Zinnia, Valley's Voice
     *  - Battlefield A: lands for {2}{W} (2 Plains, 2 generic is fine)
     *  - Hand A: Silvercoat Lion (2/2 Creature — Cat)
     * We cast Silvercoat Lion and pay offspring {2}.
     * Expected:
     *  - Two Silvercoat Lions on battlefield:
     *      - 1 original (2/2)
     *      - 1 offspring token copy (1/1, but same name & types)
     * NOTE: Depending on how Offspring prompts are implemented in XMage,
     * you may need to add one or more setChoice(...) calls here.
     */
    @Test
    public void testOffspringGrantedToCreatureSpells() {
        // Zinnia in play
        addCard(Zone.BATTLEFIELD, playerA, "Zinnia, Valley's Voice", 1);

        // Mana for Silvercoat Lion (1W) + offspring {2}
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1); // or any other land for generic mana

        // Creature spell with no printed offspring
        addCard(Zone.HAND, playerA, "Silvercoat Lion", 1);

        // Cast the Lion. Offspring {2} should be available because of Zinnia.
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Silvercoat Lion");

        // If Offspring asks a yes/no question like "Pay offspring cost {2}?"
        // you may need one of these lines (uncomment the one that matches
        // the real UI in your Offspring implementation):
        //
        setChoice(playerA, true);
        // setChoice(playerA, "Yes");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // We should have two Silvercoat Lions:
        // - original spell
        // - 1/1 token copy from offspring
        assertPermanentCount(playerA, "Silvercoat Lion", 2);
    }

    /**
     * Creature spells you cast have offspring {2}, but paying offspring is optional.
     * If we choose not to pay it, only the original creature should enter.
     */
    @Test
    public void testOffspringGrantedToCreatureSpellsNoPay() {
        addCard(Zone.BATTLEFIELD, playerA, "Zinnia, Valley's Voice", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.HAND, playerA, "Silvercoat Lion", 1);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Silvercoat Lion");
        setChoice(playerA, false);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Silvercoat Lion", 1);
    }

    private void checkPrintedAndGrantedOffspringChoices(boolean firstChoice, boolean secondChoice, int expectedBandits) {
        addCard(Zone.BATTLEFIELD, playerA, "Zinnia, Valley's Voice", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 6);
        addCard(Zone.HAND, playerA, "Prosperous Bandit", 1); // printed Offspring {1}; Zinnia grants Offspring {2}

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Prosperous Bandit");
        setChoice(playerA, firstChoice);
        setChoice(playerA, secondChoice);

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Prosperous Bandit", expectedBandits);
        assertTokenCount(playerA, "Prosperous Bandit", expectedBandits - 1);
    }

    @Test
    public void testOffspringPrintedAndGrantedChooseNoNo() {
        checkPrintedAndGrantedOffspringChoices(false, false, 1);
    }

    @Test
    public void testOffspringPrintedAndGrantedChooseYesNo() {
        checkPrintedAndGrantedOffspringChoices(true, false, 2);
    }

    @Test
    public void testOffspringPrintedAndGrantedChooseNoYes() {
        checkPrintedAndGrantedOffspringChoices(false, true, 2);
    }

    @Test
    public void testOffspringPrintedAndGrantedChooseYesYes() {
        checkPrintedAndGrantedOffspringChoices(true, true, 3);
    }
}
