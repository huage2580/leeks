package bean;

import java.util.List;

public class YahooResponse {
    Result quoteResponse;

    public YahooResponse() {
    }

    public Result getQuoteResponse() {
        return quoteResponse;
    }

    public static class Result{
        public Result() {
        }

        private List<CoinBean> result;

        public List<CoinBean> getResult() {
            return result;
        }
    }
}

