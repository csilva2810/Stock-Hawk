package rajan.udacity.stock.hawk.ui.main;

import javax.inject.Inject;

import rajan.udacity.stock.hawk.data.DataManager;
import rajan.udacity.stock.hawk.data.model.Quote;
import rajan.udacity.stock.hawk.data.model.Stock;
import rajan.udacity.stock.hawk.injection.ConfigPersistent;
import rajan.udacity.stock.hawk.ui.base.BasePresenter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@ConfigPersistent
public class MainPresenter extends BasePresenter<MainMvpView> {

    private final DataManager mDataManager;
    private CompositeSubscription mSubscriptions;

    private Boolean stockExist = false;

    @Inject
    public MainPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mSubscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(MainMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
        mSubscriptions.unsubscribe();
    }

    public void loadStocks() {
        checkViewAttached();
        mSubscriptions.add(mDataManager.getStocks()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Stock>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error loading the stocks.");
                        getMvpView().showError();
                    }

                    @Override
                    public void onNext(Stock stocks) {
                        showStocks(stocks);
                    }
                }));
    }

    public void deleteStock(String symbol) {
        checkViewAttached();
        mSubscriptions.add(mDataManager.deleteStock(symbol)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Stock>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error deleting the stock");
                        getMvpView().showError();
                    }

                    @Override
                    public void onNext(Stock stock) {
                        showStocks(stock);
                    }
                })
        );
    }

    public void showStocks(Stock stocks) {
        if (stocks == null) {
            getMvpView().showStocksEmpty();
        } else {
            getMvpView().showStocks(stocks);
        }
    }

    public Boolean checkStocksExistOrNot(final String symbol, Stock stock) {
        Observable.from(stock.getQuery().getResult().getQuote())
                .filter(new Func1<Quote, Boolean>() {
                    @Override
                    public Boolean call(Quote quote) {
                        return (quote.getMsymbol().equals(symbol));
                    }
                })
                .subscribe(new Action1<Quote>() {
                    @Override
                    public void call(Quote quote) {
                        stockExist = true;
                    }
                });
        return stockExist;
    }
}
