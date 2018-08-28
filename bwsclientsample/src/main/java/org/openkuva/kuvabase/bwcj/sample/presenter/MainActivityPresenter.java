/*
 * Copyright (c)  2018 One Kuva LLC, known as OpenKuva.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the disclaimer
 * below) provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *      * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *      * Neither the name of the One Kuva LLC, known as OpenKuva.org nor the names of its
 *      contributors may be used to endorse or promote products derived from this
 *      software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY
 * THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.openkuva.kuvabase.bwcj.sample.presenter;

import org.bitcoinj.core.ECKey;
import org.openkuva.kuvabase.bwcj.data.entity.interfaces.credentials.ICredentials;
import org.openkuva.kuvabase.bwcj.data.entity.interfaces.transaction.ITransactionProposal;
import org.openkuva.kuvabase.bwcj.data.entity.interfaces.wallet.IWallet;
import org.openkuva.kuvabase.bwcj.data.entity.pojo.transaction.CustomData;
import org.openkuva.kuvabase.bwcj.domain.useCases.exchange.getRate.IGetRateUseCases;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.addNewTxp.IAddNewTxpUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.broadcastTxp.IBroadcastTxpUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.deleteAllPendingTxProposals.IDeleteAllPendingTxpsUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.publishTxp.IPublishTxpUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.signTxp.ISignTxpUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.createWallet.ICreateWalletUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.getWalletAddress.IGetWalletAddressesUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.getWalletBalance.IGetWalletBalanceUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.joinWalletInCreation.IJoinWalletInCreationUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.recoveryWalletFromMnemonic.IRecoveryWalletFromMnemonicUseCase;
import org.openkuva.kuvabase.bwcj.sample.view.IMainActivityView;
import org.openkuva.kuvabase.bwcj.service.bitcoreWalletService.interfaces.address.IAddressesResponse;
import org.openkuva.kuvabase.bwcj.service.rate.interfaces.IRateResponse;

import static org.openkuva.kuvabase.bwcj.domain.utils.ListUtils.split;

public class MainActivityPresenter implements IMainActivityPresenter {
    private final IMainActivityView view;
    private final ICredentials credentials;

    private final ICreateWalletUseCase createWallet;
    private final IJoinWalletInCreationUseCase joinWalletInCreationUseCase;
    private final IGetWalletAddressesUseCase addressesUseCases;
    private final IGetWalletBalanceUseCase getWalletBalanceUseCases;
    private final IGetRateUseCases getRateUseCases;
    private final IAddNewTxpUseCase postTransaction;
    private final IPublishTxpUseCase publishTxpUseCase;
    private final ISignTxpUseCase signTxpUseCase;
    private final IBroadcastTxpUseCase broadcastTxpUseCase;
    private final IDeleteAllPendingTxpsUseCase deleteAllPendingTxpsUseCase;
    private final IRecoveryWalletFromMnemonicUseCase recoveryWalletFromMnemonicUseCase;

    public MainActivityPresenter(
            IMainActivityView view,
            ICredentials credentials,
            ICreateWalletUseCase createWallet,
            IJoinWalletInCreationUseCase joinWalletInCreationUseCase,
            IGetWalletAddressesUseCase addressesUseCases,
            IGetWalletBalanceUseCase getWalletBalanceUseCases,
            IGetRateUseCases getRateUseCases,
            IAddNewTxpUseCase postTransaction,
            IPublishTxpUseCase publishTxpUseCase,
            ISignTxpUseCase signTxpUseCase,
            IBroadcastTxpUseCase broadcastTxpUseCase,
            IDeleteAllPendingTxpsUseCase deleteAllPendingTxpsUseCase,
            IRecoveryWalletFromMnemonicUseCase recoveryWalletFromMnemonicUseCase) {

        this.view = view;
        this.credentials = credentials;
        this.createWallet = createWallet;
        this.joinWalletInCreationUseCase = joinWalletInCreationUseCase;
        this.addressesUseCases = addressesUseCases;
        this.getWalletBalanceUseCases = getWalletBalanceUseCases;
        this.getRateUseCases = getRateUseCases;
        this.postTransaction = postTransaction;
        this.publishTxpUseCase = publishTxpUseCase;
        this.signTxpUseCase = signTxpUseCase;
        this.broadcastTxpUseCase = broadcastTxpUseCase;
        this.deleteAllPendingTxpsUseCase = deleteAllPendingTxpsUseCase;
        this.recoveryWalletFromMnemonicUseCase = recoveryWalletFromMnemonicUseCase;
    }

    @Override
    public void createWallet() {
        try {
            String walletId = createWallet.execute();
            joinWalletInCreationUseCase.execute(walletId);
            view.updateWalletId(walletId);
        } catch (Exception e) {
            view.showMessage(e.getMessage());
        }
    }

    @Override
    public void getAddress() {
        try {
            IAddressesResponse response = addressesUseCases.execute();
            view.updateWalletAddress(response.getAddress());
        } catch (Exception e) {
            view.showMessage(e.getMessage());
        }
    }

    @Override
    public void getBalance() {
        try {
            IWallet wallet = getWalletBalanceUseCases.execute();
            view.updateWalletBalance(String.valueOf(wallet.getBalance().getAvailableAmount()));
        } catch (Exception e) {
            view.showMessage(e.getMessage());
        }
    }


    @Override
    public void sendDashToAddress(String address, String dash, String msg) {
        try {
            IRateResponse rate = getRateUseCases.execute();
            ITransactionProposal proposal =
                    postTransaction.execute(
                            address,
                            dash,
                            msg,
                            false,
                            new CustomData(
                                    Double.parseDouble(rate.getRate()),
                                    "send",
                                    null,
                                    null));
            ITransactionProposal publishedTxp = publishTxpUseCase.execute(proposal);
            ITransactionProposal signTxp = signTxpUseCase.execute(publishedTxp);
            broadcastTxpUseCase.execute(signTxp.getId());
            view.updateSendDashResult("Tx done!");
        } catch (Exception e) {
            view.showMessage(e.getMessage());
        }
    }

    @Override
    public void onRecovery(String mnemonic) {
        try {
            IWallet response =
                    recoveryWalletFromMnemonicUseCase.execute(split(mnemonic), new ECKey());

            view.updateWalletBalance(
                    String.valueOf(
                            response
                                    .getBalance()
                                    .getAvailableAmount()));
            view.updateWalletId(
                    response
                            .getWalletCore()
                            .getId());
        } catch (Exception e) {
            view.showMessage(e.getMessage());
        }
    }

    @Override
    public void deleteAllPendingTxp() {
        deleteAllPendingTxpsUseCase.execute();
    }
}
