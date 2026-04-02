package fr.sdv.seeddreamvalley.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de la classe Plot.
 * Vérifie les transitions d'état et la logique de récolte.
 */
class PlotTest {

    private Plot plot;

    @BeforeEach
    void setUp() {
        plot = new Plot(5, 5);
    }

    private void fairePoussreJusquaGrown() {
        plot.plant();
        plot.update(11f); // SEEDED → SPROUT
        plot.update(11f); // SPROUT → GROWN
    }

    /** Une parcelle est vide à la création. */
    @Test
    void devraitEtreVideALaCreation() {
        assertTrue(plot.isEmpty());
    }

    /** Les coordonnées sont correctement stockées. */
    @Test
    void devraitStockerLesCoordonnees() {
        assertEquals(5, plot.tileX);
        assertEquals(5, plot.tileY);
    }

    /** Planter passe la parcelle en SEEDED. */
    @Test
    void devraitPasserEnSeededApresPlant() {
        plot.plant();
        assertFalse(plot.isEmpty());
        assertFalse(plot.isGrown());
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
        plot.update(11f);
        assertFalse(plot.isEmpty());
        assertFalse(plot.isGrown());
        assertFalse(plot.harvest());
    }

    /** Après 10 secondes, la parcelle passe en SPROUT. */
    @Test
    void devraitPasserEnSproutApres10Secondes() {
        plot.plant();
        plot.update(11f);
        assertFalse(plot.isEmpty());
        assertFalse(plot.isGrown());
    }

    /** Après 20 secondes au total, la parcelle passe en GROWN. */
    @Test
    void devraitPasserEnGrownApres20Secondes() {
        fairePoussreJusquaGrown();
        assertTrue(plot.isGrown());
    }

    /** Une parcelle vide ne grandit pas. */
    @Test
    void neDevraitPasGrandirSiVide() {
        plot.update(999f);
        assertTrue(plot.isEmpty());
    }

    /** Une parcelle GROWN ne grandit plus même avec le temps. */
    @Test
    void neDevraitPlusGrandirSiGrown() {
        fairePoussreJusquaGrown();
        assertTrue(plot.isGrown());
        plot.update(999f);
        assertTrue(plot.isGrown());
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
        assertTrue(plot.isEmpty());
    }

    /** Après récolte, on peut replanter. */
    @Test
    void devraitPouvoirReplanterApresRecolte() {
        fairePoussreJusquaGrown();
        plot.harvest();
        plot.plant();
        assertFalse(plot.isEmpty());
        assertFalse(plot.isGrown());
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
        assertFalse(plot.isEmpty());

        // Pousser
        plot.update(11f);
        assertFalse(plot.isEmpty());
        assertFalse(plot.isGrown());

        // Mûrir
        plot.update(11f);
        assertTrue(plot.isGrown());

        // Récolter
        assertTrue(plot.harvest());
        assertTrue(plot.isEmpty());

        // Replanter
        plot.plant();
        assertFalse(plot.isEmpty());
    }
}