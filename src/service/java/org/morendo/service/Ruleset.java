package org.morendo.service;

public interface Ruleset {
    void setContents(String content);

    String getContents();

    void setURL(String url);

    String getURL();

    boolean loadRuleset(org.morendo.rete.Rete engine);

    boolean reloadRuleset(org.morendo.rete.Rete engine);
}
