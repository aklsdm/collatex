package com.sd_editions.collatex.permutations;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.output.Column;

public class Superbase extends Witness {
  private final List<Column> columnForEachWord;

  public Superbase() {
    this.columnForEachWord = Lists.newArrayList();
  }

  public void addWord(Word word, Column column) {
    getWords().add(word);
    columnForEachWord.add(column);
  }

  public Column getColumnFor(Word word) {
    int indexOf = getWords().indexOf(word);
    return columnForEachWord.get(indexOf);
  }

  //  public void setColumn(int position, Column column) {
  //    columnForEachWord.set(position - 1, column);
  //  }
}