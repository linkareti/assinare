package com.linkare.assinare.id;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;

import com.linkare.assinare.commons.AssinareException;
import com.linkare.assinare.commons.utils.MOCCAUtils;

import at.gv.egiz.smcc.CardDataSet;
import at.gv.egiz.smcc.CardNotSupportedException;
import at.gv.egiz.smcc.SignatureCard;
import at.gv.egiz.smcc.SignatureCardFactory;

/**
 *
 * @author Ricardo Vaz - Linkare TI
 */
public class CardUtils {

    private static final Logger LOG = Logger.getLogger(CardUtils.class.getName());

    private static final SignatureCardFactory SIG_CARD_FACTORY = SignatureCardFactory.getInstance();

    private static final Set<CardDataSet> REQUIRED_CARD_DATA_SETS
            = EnumSet.of(CardDataSet.HOLDER_DATA, CardDataSet.HOLDER_PICTURE, CardDataSet.HOLDER_ADDRESS);

    private CardUtils() {
    }

    public static SignatureCard getDataProvidingCard() throws AssinareException {
        Map<CardTerminal, Card> allAvailableCards = MOCCAUtils.getAllAvailableCards();

        List<SignatureCard> compatibleSigCards = new ArrayList<>();

        if (!allAvailableCards.isEmpty()) {
            for (Map.Entry<CardTerminal, Card> cardEntry : allAvailableCards.entrySet()) {
                try {
                    final SignatureCard card = SIG_CARD_FACTORY.createSignatureCard(cardEntry.getValue(), cardEntry.getKey());

                    if (card.getSupportedCardDataSets().containsAll(REQUIRED_CARD_DATA_SETS)) {
                        compatibleSigCards.add(card);
                    }
                } catch (CardNotSupportedException ex) {
                    LOG.log(Level.INFO, "Cartão não reconhecido", ex);
                }
            }

            if (compatibleSigCards.isEmpty()) {
                throw new AssinareException("Nenhum dos cartões encontrados é compatível com esta aplicação.");
            } else if (compatibleSigCards.size() > 1) {
                throw new AssinareException("Foram encontrados mais do que um cartão compatível com esta aplicação.");
            } else {
                return compatibleSigCards.get(0);
            }
        } else {
            throw new AssinareException("Não existem cartões inseridos no(s) leitor(es).");
        }
    }

}
