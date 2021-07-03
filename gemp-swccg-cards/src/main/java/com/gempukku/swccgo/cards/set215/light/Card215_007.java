package com.gempukku.swccgo.cards.set215.light;

import com.gempukku.swccgo.cards.AbstractSite;
import com.gempukku.swccgo.cards.GameConditions;
import com.gempukku.swccgo.common.*;
import com.gempukku.swccgo.filters.Filter;
import com.gempukku.swccgo.filters.Filters;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.game.SwccgGame;
import com.gempukku.swccgo.logic.TriggerConditions;
import com.gempukku.swccgo.logic.actions.RequiredGameTextTriggerAction;
import com.gempukku.swccgo.logic.actions.TopLevelGameTextAction;
import com.gempukku.swccgo.logic.effects.*;
import com.gempukku.swccgo.logic.timing.EffectResult;
import com.gempukku.swccgo.logic.timing.PassthruEffect;
import com.gempukku.swccgo.logic.timing.results.AboutToLeaveTableResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Set 15
 * Type: Location
 * Subtype: Site
 * Title: Death Star: Detention Block Corridor (V)
 */
public class Card215_007 extends AbstractSite {
    public Card215_007() {
        super(Side.LIGHT, Title.Detention_Block_Corridor, Title.Death_Star);
        setVirtualSuffix(true);
        setLocationDarkSideGameText("If Leia is about to be lost from a Death Star site, imprison her here instead (even if inactive).");
        setLocationLightSideGameText("If you control with a spy, may use 2 Force to release Leia here (retrieve 1 Force).");
        addIcon(Icon.DARK_FORCE, 1);
        addIcons(Icon.SPECIAL_EDITION, Icon.INTERIOR_SITE, Icon.MOBILE, Icon.SCOMP_LINK, Icon.VIRTUAL_SET_15);
        addKeywords(Keyword.PRISON);
    }

    @Override
    protected List<TopLevelGameTextAction> getGameTextLightSideTopLevelActions(String playerOnLightSideOfLocation, SwccgGame game, PhysicalCard self, int gameTextSourceCardId) {
        Filter captiveLeiaHere = Filters.and(Filters.Leia, Filters.here(self), Filters.captive);

        if (GameConditions.controlsWith(game, playerOnLightSideOfLocation, self, Filters.spy)
                && GameConditions.canSpot(game, self, SpotOverride.INCLUDE_CAPTIVE, captiveLeiaHere)
                && GameConditions.canUseForce(game, playerOnLightSideOfLocation, 2)) {
            TopLevelGameTextAction action = new TopLevelGameTextAction(self, playerOnLightSideOfLocation, gameTextSourceCardId);
            action.setText("Use 2 Force to release Leia here");
            action.appendCost(
                    new UseForceEffect(action, playerOnLightSideOfLocation, 2)
            );
            action.appendEffect(
                    new ReleaseCaptiveEffect(action, Filters.findFirstActive(game, self, SpotOverride.INCLUDE_CAPTIVE, captiveLeiaHere))
            );
            action.appendEffect(
                    new RetrieveForceEffect(action, playerOnLightSideOfLocation, 1)
            );
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    protected List<RequiredGameTextTriggerAction> getGameTextDarkSideRequiredAfterTriggers(String playerOnDarkSideOfLocation, SwccgGame game, EffectResult effectResult, final PhysicalCard self, int gameTextSourceCardId) {
        if (TriggerConditions.isAboutToLeaveTable(game, effectResult, Filters.and(Filters.at(Filters.Death_Star_site), Filters.Leia))) {
            final AboutToLeaveTableResult result = (AboutToLeaveTableResult) effectResult;
            final PhysicalCard leia = result.getCardAboutToLeaveTable();

            if (leia != null) {
                final RequiredGameTextTriggerAction action = new RequiredGameTextTriggerAction(self, gameTextSourceCardId);
                action.setText("Imprison Leia");
                action.setPerformingPlayer(playerOnDarkSideOfLocation);
                action.appendEffect(
                        new PassthruEffect(action) {
                            @Override
                            protected void doPlayEffect(SwccgGame game) {
                                result.getPreventableCardEffect().preventEffectOnCard(leia);
                                action.appendEffect(
                                        new RestoreCardToNormalEffect(action, leia));
                                action.appendEffect(
                                        new CaptureWithImprisonmentEffect(action, leia, self, leia.isUndercover(), leia.isMissing())
                                );
                            }
                        });
                return Collections.singletonList(action);
            }

        }
        return null;
    }
}
