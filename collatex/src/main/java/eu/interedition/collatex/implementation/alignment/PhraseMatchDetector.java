/*
 * Copyright 2011 The Interedition Development Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Ronald
 */
public class PhraseMatchDetector {

  public List<Tuple<List<Token>>> detect(Map<Token, Token> linkedTokens, IWitness base, IWitness witness) {
    //rank the variant graph
    VariantGraphWitnessAdapter adapter = (VariantGraphWitnessAdapter) base;
    adapter.getGraph().rank();
    
    List<Tuple<List<Token>>> phraseMatches = Lists.newArrayList();

    // gather matched ranks into a set ordered by their natural order
    Set<Integer> rankSet = Sets.newTreeSet();
    for (Token baseToken: linkedTokens.values()) {
      VariantGraphVertexTokenAdapter a = (VariantGraphVertexTokenAdapter) baseToken;
      int rank = a.getVertex().getRank();
      rankSet.add(rank);
    }
 
    //Turn it into a List so that distance between matched ranks can be called
    //Note that omitted vertices are not in the list, so they don't cause an extra phrasematch
    List<Integer> ranks = Lists.newArrayList(rankSet);

    // chain token matches
    List<Token> basePhrase = Lists.newArrayList();
    List<Token> witnessPhrase = Lists.newArrayList();
    int previousRank = 1;

    for (Token token : witness.getTokens()) {
      //Note: this if skips added tokens so they don't cause an extra phrasematch
      if (!linkedTokens.containsKey(token)) {
        continue;
      }
      Token baseToken = linkedTokens.get(token);
      VariantGraphVertexTokenAdapter a = (VariantGraphVertexTokenAdapter) baseToken;
      int rank = a.getVertex().getRank();
      int indexOfRank = ranks.indexOf(rank);
      int indexOfPreviousRank = ranks.indexOf(previousRank);
      int difference = indexOfRank - indexOfPreviousRank;
      if (difference != 0 && difference != 1) {
        if (!basePhrase.isEmpty()) {
          // start a new sequence
          phraseMatches.add(new Tuple<List<Token>>(Lists.newArrayList(basePhrase), Lists.newArrayList(witnessPhrase)));
        }
        // clear buffer
        basePhrase.clear();
        witnessPhrase.clear();
      }
      basePhrase.add(baseToken);
      witnessPhrase.add(token);
      previousRank = rank;
    }
    if (!basePhrase.isEmpty()) {
      phraseMatches.add(new Tuple<List<Token>>(Lists.newArrayList(basePhrase), Lists.newArrayList(witnessPhrase)));
    }
    return phraseMatches;
  }
}
