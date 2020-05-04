package id.ac.nitin.apicovid;

public class GlobalCovidData {
    public String globalConfirmerdCases;
    public String globalNewConfirmerdCases;
    public String globalDeaths;
    public String globalNewDeaths;
    public String globalRecovered;
    public String globalNewRecovered;
    public String date;

    //Default constructer
    public GlobalCovidData(){
    }

    public GlobalCovidData(String confirmedCases  , String newConfirmedCases,String deaths,
                           String newdeaths, String recovered , String newRecovered ,
                           String date ){
        this.globalConfirmerdCases = confirmedCases;
        this.globalNewConfirmerdCases = newConfirmedCases;
        this.globalDeaths = deaths;
        this.globalNewDeaths = newdeaths;
        this.globalRecovered = recovered;
        this.globalNewRecovered = newRecovered;
        this.date = date;
    }
}





