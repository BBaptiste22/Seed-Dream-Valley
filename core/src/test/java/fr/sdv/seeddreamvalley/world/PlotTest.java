package fr.sdv.seeddreamvalley.world;

import fr.sdv.seeddreamvalley.world.Plot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de la classe Plot.
 * Vérifie les transitions d'état et la logique de récolte.
 */
class PlotTest {

    private Plot plot;

    /** Crée une parcelle fraîche avant chaque test. */
    @BeforeEach
    void setUp() {
        plot = new Plot(5, 5);
    }


    /** Fait pousser la parcelle jusqu'à GROWN (plante + 2x 11 secondes). */
    private void fairePoussreJusquaGrown() {
        plot.plant();
        plot.update(11f); // SEEDED → SPROUT
        plot.update(11f); // SPROUT → GROWN
    }


    /** Une parcelle est vide à la création. */
    @Test
    void devraitEtreVideALaCreation() {
        assertEquals(Plot.STAGE_EMPTY, plot.getStage());
    }

    /** Les coordonnées sont correctement stockées. */
    @Test
    void devraitStockerLesCoordonnees() {
        assertEquals(5, plot.tileX);
        assertEquals(5, plot.tileY);
    }

    // ── Planter ──────────────────────────────────────────────────────

    /** Planter passe la parcelle en SEEDED. */
    @Test
    void devraitPasserEnSeededApresPlant() {
        plot.plant();
        assertEquals(Plot.STAGE_SEEDED, plot.getStage());
    }

    /** On ne peut pas récolter une parcelle vide. */
    @Test
    void neDevraitPasRecolterSiVide() {
        assertFalse(plot.harvest());
    }

    /** On ne peut pas récolter une parcelle juste plantée. */
    @Test
    void neDevraitPasRecolterSiSeeded() {
        plot.plant();
        assertFalse(plot.harvest());
    }

    /** On ne peut pas récolter une parcelle en train de pousser. */
    @Test
    void neDevraitPasRecolterSiSprout() {
        plot.plant();
        plot.update(11f); // SPROUT
        assertFalse(plot.harvest());
    }


    /** Après 10 secondes, la parcelle passe en SPROUT. */
    @Test
    void devraitPasserEnSproutApres10Secondes() {
        plot.plant();
        plot.update(11f);
        assertEquals(Plot.STAGE_SPROUT, plot.getStage());
    }

    /** Après 20 secondes au total, la parcelle passe en GROWN. */
    @Test
    void devraitPasserEnGrownApres20Secondes() {
        fairePoussreJusquaGrown();
        assertEquals(Plot.STAGE_GROWN, plot.getStage());
    }

    /** Une parcelle vide ne grandit pas. */
    @Test
    void neDevraitPasGrandirSiVide() {
        plot.update(999f);
        assertEquals(Plot.STAGE_EMPTY, plot.getStage());
    }

    /** Une parcelle GROWN ne grandit plus même avec le temps. */
    @Test
    void neDevraitPlusGrandirSiGrown() {
        fairePoussreJusquaGrown();
        assertEquals(Plot.STAGE_GROWN, plot.getStage());
        plot.update(999f);
        assertEquals(Plot.STAGE_GROWN, plot.getStage());
    }

    /** On peut récolter une parcelle GROWN. */
    @Test
    void devraitRecolterSiGrown() {
        fairePoussreJusquaGrown();
        assertTrue(plot.harvest());
    }

    /** Après récolte, la parcelle redevient vide. */
    @Test
    void devraitRedevenirVideApresRecolte() {
        fairePoussreJusquaGrown();
        plot.harvest();
        assertEquals(Plot.STAGE_EMPTY, plot.getStage());
    }

    /** Après récolte, on peut replanter. */
    @Test
    void devraitPouvoirReplanterApresRecolte() {
        fairePoussreJusquaGrown();
        plot.harvest();
        plot.plant();
        assertEquals(Plot.STAGE_SEEDED, plot.getStage());
    }

    /** harvest() retourne false si appelé deux fois. */
    @Test
    void neDevraitPasRecolterDeuxFois() {
        fairePoussreJusquaGrown();
        assertTrue(plot.harvest());
        assertFalse(plot.harvest());
    }


    /** Cycle complet : planter → sprout → grown → récolter → replanter. */
    @Test
    void devraitFaireLeCycleComplet() {
        // Planter
        plot.plant();
        assertEquals(Plot.STAGE_SEEDED, plot.getStage());

        // Pousser
        plot.update(11f);
        assertEquals(Plot.STAGE_SPROUT, plot.getStage());

        // Mûrir
        plot.update(11f);
        assertEquals(Plot.STAGE_GROWN, plot.getStage());

        // Récolter
        assertTrue(plot.harvest());
        assertEquals(Plot.STAGE_EMPTY, plot.getStage());

        // Replanter
        plot.plant();
        assertEquals(Plot.STAGE_SEEDED, plot.getStage());
    }
}