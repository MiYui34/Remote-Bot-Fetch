package com.sguserver.remotebotfetch.client.gui;

/**
 * Duck interface implemented by the mixins on the vanilla screens, so that
 * every place which needs the fetch panel of a screen can reach the very same
 * panel instance without keeping a global map of screens around.
 */
public interface RbfPanelHost {
    RbfFetchPanel rbf$getPanel();

    /**
     * Re-positions the screen for the given panel state: the inventory is shifted
     * to the right while the panel is open, exactly like the vanilla recipe book
     * does it.
     */
    void rbf$updatePanelPosition(boolean open);

    /**
     * Moves the vanilla recipe book button to the position it has to have for the
     * current {@code leftPos}. The vanilla code only does this from its own click
     * handler, so shifting the screen without this would leave the button behind.
     */
    void rbf$moveRecipeButton();
}
