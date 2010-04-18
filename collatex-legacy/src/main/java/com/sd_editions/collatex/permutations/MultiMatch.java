package com.sd_editions.collatex.permutations;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import eu.interedition.collatex.input.Word;

public class MultiMatch {
  public final String name;
  private final Multimap<String, Word> similarWordsPerWitness;

  public MultiMatch(Word... witnessWords) {
    this.similarWordsPerWitness = ArrayListMultimap.create();
    this.name = witnessWords[0]._normalized;
    for (Word word : witnessWords) {
      addMatchingWord(word);
    }
  }

  public void addMatchingWord(Word word) {
    if (word._normalized.equals(this.name)) {
      similarWordsPerWitness.get(word.getWitnessId()).add(word);
    }
  }

  public List<Word> getOccurancesInWitness(String witnessId) {
    return (List<Word>) similarWordsPerWitness.get(witnessId);
  }

  public List<Word> getWords() {
    return (List<Word>) similarWordsPerWitness.values();
  }

  /*
   * matches01 = determine matches in witness 0,1
   * initialMatchSet = matches01
   * matches01.collect{||} 
   * matches02 = determine matches in witness 0,2
   * supermatches = intersection of supermatches and matches02 (just based on the normalized words)
   * .. etc.
   * 
   * 
   */

}
