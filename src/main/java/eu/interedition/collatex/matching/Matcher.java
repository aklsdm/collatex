package eu.interedition.collatex.matching;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.permutations.MatchGroup;

import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.collation.NonMatch;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class Matcher {

  public Result match(Witness a, Witness b) {
    Set<Match> allMatches = findMatches(a, b);
    Set<MatchGroup> possibleMatches = Sets.newLinkedHashSet();
    // group matches by common base word or common witness word
    Set<Match> exactMatches = Sets.newLinkedHashSet();
    for (Match match : allMatches) {
      Iterable<Match> alternatives = findAlternatives(allMatches, match);
      if (!alternatives.iterator().hasNext()) {
        exactMatches.add(match);
      } else {
        // start MatchGroup van de iterator
        MatchGroup group = new MatchGroup();
        group.add(match);
        group.addAll(alternatives);
        possibleMatches.add(group);
      }
    }
    return new Result(exactMatches, possibleMatches);
  }

  private Set<Match> findMatches(Witness base, Witness witness) {
    Set<Match> matchSet = Sets.newLinkedHashSet();
    for (Word baseWord : base.getWords()) {
      for (Word witnessWord : witness.getWords()) {
        if (baseWord.normalized.equals(witnessWord.normalized)) {
          matchSet.add(new Match(baseWord, witnessWord));
          //        } else {
          //          float editDistance = distanceMeasure.distance(baseWord.normalized, witnessWord.normalized);
          //          if (editDistance < 0.5) matchSet.add(new Match(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }

  Iterable<Match> findAlternatives(Iterable<Match> pmatches, final Match pmatch) {
    Predicate<Match> unfixedAlternativeToGivenPMatch = new Predicate<Match>() {
      public boolean apply(Match pm) {
        return pm != pmatch && (pm.getBaseWord().equals(pmatch.getBaseWord()) /* || pm.getWitnessWord().equals(pmatch.getWitnessWord())*/);
      }
    };
    return Iterables.filter(pmatches, unfixedAlternativeToGivenPMatch);
  }

  // TODO: rename, test separately
  public List<Permutation> getPossiblePermutationsForMatchGroup(Witness a, Witness b, int i) {
    Result result = match(a, b);
    Set<Match> fixedMatches = result.getExactMatches();
    Set<MatchGroup> matchGroupsForPossibleMatches = result.getPossibleMatches();
    Iterator<MatchGroup> iterator = matchGroupsForPossibleMatches.iterator();
    MatchGroup matchGroup = iterator.next();
    List<Permutation> permutations = getPermutationsForMatchGroup(fixedMatches, matchGroup);
    Permutation bestPermutation = selectBestPossiblePermutation(a, b, permutations);
    fixedMatches.add(bestPermutation.getPossibleMatch());
    // TODO: filter away possible matches which contain position which are occupied by the chosen permutation
    matchGroupsForPossibleMatches = filterAwayNoLongerPossibleMatches(bestPermutation.getPossibleMatch(), matchGroupsForPossibleMatches);
    iterator = matchGroupsForPossibleMatches.iterator();
    matchGroup = iterator.next();
    //    matchGroup = iterator.next();
    //    matchGroup = iterator.next();
    System.out.println(matchGroup);
    permutations = getPermutationsForMatchGroup(fixedMatches, matchGroup);
    bestPermutation = selectBestPossiblePermutation(a, b, permutations);
    System.out.println(bestPermutation.getPossibleMatch());
    System.out.println(bestPermutation.getMatchSequences());
    System.out.println(bestPermutation.getNonMatches(a, b)); // THIS ONE GOES WRONG!
    return permutations;
  }

  private Set<MatchGroup> filterAwayNoLongerPossibleMatches(Match selectedMatch, Set<MatchGroup> matchGroupsForPossibleMatches) {
    Set<MatchGroup> results = Sets.newLinkedHashSet();
    for (MatchGroup group : matchGroupsForPossibleMatches) {
      MatchGroup newGroup = new MatchGroup();
      for (Match match : group) {
        if (match.getBaseWord().equals(selectedMatch.getBaseWord()) || match.getWitnessWord().equals(selectedMatch.getWitnessWord())) {
          // do nothing... this one should be filtered away
        } else {
          newGroup.add(match);
        }
      }
      if (!newGroup.isEmpty()) {
        results.add(newGroup);
      }
    }
    return results;
  }

  private List<Permutation> getPermutationsForMatchGroup(Set<Match> exactMatches, MatchGroup matchGroup) {
    List<Permutation> permutationsForMatchGroup = Lists.newArrayList();
    for (Match possibleMatch : matchGroup) {
      Permutation permutation = new Permutation(exactMatches, possibleMatch);
      //      Set<Match> permutation = Sets.newLinkedHashSet();
      //      permutation.addAll(exactMatches);
      //      permutation.add(possibleMatch);
      // System.out.println(permutation);
      permutationsForMatchGroup.add(permutation);
    }
    return permutationsForMatchGroup;
  }

  private Permutation selectBestPossiblePermutation(Witness a, Witness b, List<Permutation> permutations) {
    Permutation bestPermutation = null;

    // NOTE: this can be done in a nicer with the min function!
    for (Permutation permutation : permutations) {
      List<NonMatch> nonMatches = permutation.getNonMatches(a, b);
      if (bestPermutation == null || nonMatches.size() < bestPermutation.getNonMatches(a, b).size()) {
        bestPermutation = permutation;
      }
    }
    if (bestPermutation == null) throw new RuntimeException("Unexpected error!");
    //System.out.println(bestPermutation.getPossibleMatch());
    return bestPermutation;
  }

}
