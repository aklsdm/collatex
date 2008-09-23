package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class Modifications {
  private final List<Transposition> transpositions;
  private final List<Modification> modifications;
  private final Set<Match> matches;

  public Modifications(List<Modification> _modifications, List<Transposition> _transpositions, Set<Match> _matches) {
    this.modifications = _modifications;
    this.transpositions = _transpositions;
    this.matches = _matches;
  }

  public List<Transposition> getTranspositions() {
    return transpositions;
  }

  public List<Modification> getModifications() {
    List<Modification> addedUp = Lists.newArrayList();
    addedUp.addAll(modifications);
    addedUp.addAll(transpositions);
    return addedUp;
  }

  public int size() {
    return getModifications().size();
  }

  public Modification get(int i) {
    return getModifications().get(i);
  }

  public Match getMatchAtBasePosition(int basePosition) {
    for (Match match : matches) {
      if (match.getBaseWord().position == basePosition) return match;
    }
    return null;
  }

}
