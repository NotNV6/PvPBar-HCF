package bar.pvp.hcf.teams.economy;

import bar.pvp.hcf.profiles.Profile;

public class Economy {

    public Integer getBalance(Profile profile) {
        return profile.getEconomy();
    }

    public void setBalance(Profile profile, Integer balance) {
        profile.setEconomy(balance);
    }

    public void addBalance(Profile profile, Integer toAdd) {
        profile.setEconomy(profile.getEconomy() + toAdd);
    }
}
