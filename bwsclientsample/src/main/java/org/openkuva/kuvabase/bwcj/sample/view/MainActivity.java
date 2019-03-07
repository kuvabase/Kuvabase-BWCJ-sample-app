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

package org.openkuva.kuvabase.bwcj.sample.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.GsonBuilder;

import org.bitcoinj.params.MainNetParams;
import org.openkuva.kuvabase.bwcj.data.entity.interfaces.credentials.ICredentials;
import org.openkuva.kuvabase.bwcj.domain.useCases.credentials.InitializeCredentialsWithRandomValueUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.exchange.getRate.GetRateUseCases;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.addNewTxp.AddNewTxpUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.broadcastTxp.BroadcastTxpUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.deleteAllPendingTxProposals.DeleteAllPendingTxpsUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.publishTxp.PublishTxpUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.transactionProposal.signTxp.SignTxpUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.createWallet.CreateWalletUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.getWalletAddress.GetWalletAddressesUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.getWalletBalance.GetWalletBalanceUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.joinWalletInCreation.JoinWalletInCreationUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.postWalletAddress.CreateNewMainAddressesFromWalletUseCase;
import org.openkuva.kuvabase.bwcj.domain.useCases.wallet.recoveryWalletFromMnemonic.RecoveryWalletFromMnemonicUseCase;
import org.openkuva.kuvabase.bwcj.domain.utils.CommonNetworkParametersBuilder;
import org.openkuva.kuvabase.bwcj.domain.utils.CopayersCryptUtils;
import org.openkuva.kuvabase.bwcj.domain.utils.DashCoinTypeRetriever;
import org.openkuva.kuvabase.bwcj.domain.utils.transactions.TransactionBuilder;
import org.openkuva.kuvabase.bwcj.sample.ApiUrls;
import org.openkuva.kuvabase.bwcj.sample.R;
import org.openkuva.kuvabase.bwcj.sample.model.credentials.CredentialsRepositoryProvider;
import org.openkuva.kuvabase.bwcj.sample.model.rate.RateApiProvider;
import org.openkuva.kuvabase.bwcj.sample.model.wallet.WalletRepositoryProvider;
import org.openkuva.kuvabase.bwcj.sample.presenter.AsyncMainActivityPresenter;
import org.openkuva.kuvabase.bwcj.sample.presenter.IMainActivityPresenter;
import org.openkuva.kuvabase.bwcj.sample.presenter.MainActivityPresenter;
import org.openkuva.kuvabase.bwcj.service.bitcoreWalletService.interfaces.IBitcoreWalletServerAPI;
import org.openkuva.kuvabase.bwcj.service.bitcoreWalletService.retrofit2.IRetrofit2BwsAPI;
import org.openkuva.kuvabase.bwcj.service.bitcoreWalletService.retrofit2.Retrofit2BwsApiBridge;
import org.openkuva.kuvabase.bwcj.service.bitcoreWalletService.retrofit2.interceptors.BWCRequestSignatureInterceptor;

import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button createWalletBtn;
    private Button getAddressBtn;
    private Button getBalanceBtn;
    private Button sendDash;

    private TextView walletIdTextTv;
    private TextView walletAddressTv;
    private TextView walletBalanceTv;
    private TextView sendDashResultTv;

    private EditText walletAddressToSendEt;
    private EditText dashToSendEt;
    private EditText messageToSendEt;

    private IMainActivityPresenter mainActivityPresenter;
    private IBitcoreWalletServerAPI bitcoreWalletServerAPI;
    private ICredentials credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupCredentials();
        setupBwsApi();
        initializeView();
        setupPresenter();
    }

    private void setupCredentials() {
        credentials = CredentialsRepositoryProvider.get(this);
        credentials.setNetworkParameters(MainNetParams.get());
    }

    private void setupPresenter() {
        mainActivityPresenter =
                new AsyncMainActivityPresenter(
                        new MainActivityPresenter(
                                new InMainThreadMainActivityView(
                                        new MainActivityView(
                                                walletIdTextTv,
                                                walletAddressTv,
                                                walletBalanceTv,
                                                sendDashResultTv,
                                                findViewById(R.id.tv_words),
                                                this),
                                        new Handler(Looper.getMainLooper())),
                                credentials,
                                new CreateWalletUseCase(
                                        credentials,
                                        new CopayersCryptUtils(
                                                new DashCoinTypeRetriever()),
                                        bitcoreWalletServerAPI),
                                new JoinWalletInCreationUseCase(
                                        credentials,
                                        bitcoreWalletServerAPI,
                                        new CopayersCryptUtils(
                                                new DashCoinTypeRetriever())),
                                new GetWalletAddressesUseCase(
                                        bitcoreWalletServerAPI),
                                new GetWalletBalanceUseCase(
                                        bitcoreWalletServerAPI,
                                        WalletRepositoryProvider.get()),
                                new GetRateUseCases(
                                        RateApiProvider.get()),
                                new AddNewTxpUseCase(
                                        bitcoreWalletServerAPI),
                                new PublishTxpUseCase(
                                        bitcoreWalletServerAPI,
                                        credentials,
                                        new TransactionBuilder(
                                                new CommonNetworkParametersBuilder()),
                                        new CopayersCryptUtils(new DashCoinTypeRetriever())),
                                new SignTxpUseCase(
                                        bitcoreWalletServerAPI,
                                        credentials,
                                        new TransactionBuilder(
                                                new CommonNetworkParametersBuilder()),
                                        new CopayersCryptUtils(new DashCoinTypeRetriever())),
                                new BroadcastTxpUseCase(
                                        bitcoreWalletServerAPI),
                                new DeleteAllPendingTxpsUseCase(
                                        bitcoreWalletServerAPI),
                                new RecoveryWalletFromMnemonicUseCase(
                                        credentials,
                                        bitcoreWalletServerAPI),
                                bitcoreWalletServerAPI,
                                new InitializeCredentialsWithRandomValueUseCase(credentials),
                                new CreateNewMainAddressesFromWalletUseCase(
                                        bitcoreWalletServerAPI)),
                        Executors.newCachedThreadPool());
    }

    private void setupBwsApi() {
        bitcoreWalletServerAPI =
                new Retrofit2BwsApiBridge(
                        new Retrofit.Builder()
                                .baseUrl(ApiUrls.URL_BWS)
                                .addConverterFactory(
                                        GsonConverterFactory.create(
                                                new GsonBuilder()
                                                        .setLenient()
                                                        .create()))
                                .client(
                                        new OkHttpClient
                                                .Builder()
                                                .addInterceptor(
                                                        new BWCRequestSignatureInterceptor(
                                                                CredentialsRepositoryProvider.get(this),
                                                                new CopayersCryptUtils(new DashCoinTypeRetriever()),
                                                                ApiUrls.URL_BWS))
                                                .addInterceptor(
                                                        new HttpLoggingInterceptor()
                                                                .setLevel(HttpLoggingInterceptor.Level.BODY))
                                                .build())
                                .build()
                                .create(IRetrofit2BwsAPI.class));
    }

    private void initializeView() {
        createWalletBtn = findViewById(R.id.ma_create_wallet_btn);
        createWalletBtn.setOnClickListener(this);
        walletIdTextTv = findViewById(R.id.ma_wallet_id_tv);

        getAddressBtn = findViewById(R.id.ma_wallet_address_btn);
        getAddressBtn.setOnClickListener(this);
        walletAddressTv = findViewById(R.id.ma_wallet_address_tv);

        getBalanceBtn = findViewById(R.id.ma_wallet_balance_btn);
        getBalanceBtn.setOnClickListener(this);
        walletBalanceTv = findViewById(R.id.ma_wallet_balance_tv);

        sendDash = findViewById(R.id.ma_send_dash_btn);
        sendDash.setOnClickListener(this);
        walletAddressToSendEt = findViewById(R.id.ma_wallet_address_to_send_et);
        dashToSendEt = findViewById(R.id.ma_dash_to_send_et);
        sendDashResultTv = findViewById(R.id.ma_send_dash_callback_tv);
        messageToSendEt = findViewById(R.id.ma_msg_to_send_et);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ma_create_wallet_btn:
                mainActivityPresenter.createWallet();
                break;
            case R.id.ma_wallet_address_btn:
                mainActivityPresenter.getAddress();
                break;
            case R.id.ma_wallet_balance_btn:
                mainActivityPresenter.getBalance();
                break;
            case R.id.ma_send_dash_btn:
                mainActivityPresenter.sendDashToAddress(
                        walletAddressToSendEt.getText().toString(),
                        dashToSendEt.getText().toString(),
                        messageToSendEt.getText().toString());
                break;
        }
    }

    public void onRecovery(View view) {
        Editable mnemonic = ((EditText) findViewById(R.id.ma_wallet_recovery_et)).getText();
        mainActivityPresenter.onRecovery(mnemonic.toString());
    }

    public void onDeleteAllPendingTxp(View view) {
        mainActivityPresenter.deleteAllPendingTxp();
    }
}
