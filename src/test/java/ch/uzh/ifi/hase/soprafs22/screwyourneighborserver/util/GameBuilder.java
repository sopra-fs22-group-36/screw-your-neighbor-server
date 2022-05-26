package ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.util;

import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.entity.*;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.ParticipationRepository;
import ch.uzh.ifi.hase.soprafs22.screwyourneighborserver.repository.PlayerRepository;
import java.util.*;
import java.util.stream.Collectors;

public class GameBuilder {

  public static GameBuilder builder(String name) {
    return builder(name, null, null, null);
  }

  public static GameBuilder builder(
      String name,
      GameRepository gameRepository,
      ParticipationRepository participationRepository,
      PlayerRepository playerRepository) {
    return new GameBuilder(name, gameRepository, participationRepository, playerRepository);
  }

  private final GameRepository gameRepository;
  private final ParticipationRepository participationRepository;
  private final PlayerRepository playerRepository;

  private final String name;
  private GameState gameState = GameState.FINDING_PLAYERS;
  private final Map<String, Participation> participationMap = new LinkedHashMap<>();
  private final List<MatchBuilder> matches = new ArrayList<>();

  public GameBuilder(
      String name,
      GameRepository gameRepository,
      ParticipationRepository participationRepository,
      PlayerRepository playerRepository) {
    this.name = name;
    this.gameRepository = gameRepository;
    this.participationRepository = participationRepository;
    this.playerRepository = playerRepository;
  }

  public GameBuilder withParticipation(String playerName) {
    participationMap.put(playerName, null);
    return this;
  }

  public GameBuilder withParticipation(Participation participation) {
    Objects.requireNonNull(participation);
    participationMap.put(participation.getPlayer().getName(), participation);
    return this;
  }

  public GameBuilder withParticipationWith(Player player) {
    String playerName = Objects.requireNonNull(player.getName());
    Participation participation = new Participation();
    participation.setPlayer(player);
    participationMap.put(playerName, participation);
    return this;
  }

  public GameBuilder withGameState(GameState gameState) {
    this.gameState = gameState;
    return this;
  }

  public MatchBuilder withMatch() {
    return MatchBuilder.builder(this);
  }

  public Game build() {
    Game game = new Game();
    game.setName(name);
    game.setGameState(gameState);

    if (gameRepository != null) {
      gameRepository.saveAll(List.of(game));
    }

    int participationNumber = 0;
    for (String name : participationMap.keySet()) {
      Participation participation = participationMap.get(name);
      if (participation == null) {
        Player player = new Player();
        player.setName(name);

        if (playerRepository != null) {
          playerRepository.saveAll(List.of(player));
        }

        participation = new Participation();
        participation.setPlayer(player);
      }
      participation.setGame(game);
      game.getParticipations().add(participation);

      if (participationRepository != null) {
        participationRepository.saveAll(List.of(participation));
      }

      participation.setParticipationNumber(participationNumber++);
      participationMap.put(name, participation);
    }
    int matchNumber = 1;
    for (MatchBuilder matchBuilder : matches) {
      Match match = matchBuilder.build(participationMap);
      match.setMatchNumber(matchNumber++);
      match.setGame(game);
      game.getMatches().add(match);
    }

    if (participationRepository != null) {
      participationRepository.saveAll(participationMap.values());
    }

    return game;
  }

  private GameBuilder addMatch(MatchBuilder matchBuilder) {
    this.matches.add(matchBuilder);
    return this;
  }

  public static class MatchBuilder {
    public static MatchBuilder builder(GameBuilder gameBuilder) {
      return new MatchBuilder(gameBuilder);
    }

    private final GameBuilder gameBuilder;

    private MatchState matchState;
    private final List<HandBuilder> hands = new ArrayList<>();
    private final List<RoundBuilder> rounds = new ArrayList<>();

    public MatchBuilder(GameBuilder gameBuilder) {
      this.gameBuilder = gameBuilder;
    }

    public MatchBuilder withMatchState(MatchState matchState) {
      this.matchState = matchState;
      return this;
    }

    public HandBuilder withHandForPlayer(String playerName) {
      return HandBuilder.builder(this, playerName);
    }

    public RoundBuilder withRound() {
      return RoundBuilder.builder(this);
    }

    public GameBuilder finishMatch() {
      return this.gameBuilder.addMatch(this);
    }

    private MatchBuilder addHand(HandBuilder handBuilder) {
      this.hands.add(handBuilder);
      return this;
    }

    private MatchBuilder addRound(RoundBuilder roundBuilder) {
      this.rounds.add(roundBuilder);
      return this;
    }

    private Match build(Map<String, Participation> participationMap) {
      Match match = new Match();
      match.setMatchState(matchState);
      Map<CardValue, Card> cardsMap = new HashMap<>();
      for (HandBuilder handBuilder : hands) {
        Hand hand = new Hand();
        hand.setMatch(match);
        match.getHands().add(hand);
        Participation participation = participationMap.get(handBuilder.playerName);
        hand.setParticipation(participation);
        participation.getHands().add(hand);
        hand.setAnnouncedScore(handBuilder.announcedScore);
        for (CardValue cardValue : handBuilder.cards) {
          Card card = new Card(cardValue.getRank(), cardValue.getCardSuit());
          card.setHand(hand);
          hand.getCards().add(card);
          cardsMap.put(cardValue, card);
        }
      }

      int roundNumber = 1;
      for (RoundBuilder roundBuilder : rounds) {
        Round round = new Round();
        round.setMatch(match);
        match.getRounds().add(round);
        round.setRoundNumber(roundNumber++);

        roundBuilder.cardsPlayed.forEach(
            (s, cardValue) -> {
              Participation participation = participationMap.get(s);
              Objects.requireNonNull(participation);
              Card card = cardsMap.get(cardValue);
              Objects.requireNonNull(card);
              String cardHolderName = card.getHand().getParticipation().getPlayer().getName();
              if (!s.equals(cardHolderName)) {
                throw new IllegalStateException(
                    "Player %s cannot play card %s".formatted(s, cardValue));
              }
              card.setRound(round);
              round.getCards().add(card);
            });
      }
      return match;
    }
  }

  public static class RoundBuilder {

    public static RoundBuilder builder(MatchBuilder matchBuilder) {
      return new RoundBuilder(matchBuilder);
    }

    private final MatchBuilder matchBuilder;
    private final Map<String, CardValue> cardsPlayed = new HashMap<>();

    public RoundBuilder(MatchBuilder matchBuilder) {
      this.matchBuilder = matchBuilder;
    }

    public RoundBuilder withPlayedCard(String playerName, CardValue cardValue) {
      this.cardsPlayed.put(playerName, cardValue);
      return this;
    }

    public MatchBuilder finishRound() {
      return matchBuilder.addRound(this);
    }
  }

  public static class HandBuilder {
    public static HandBuilder builder(MatchBuilder matchBuilder, String playerName) {
      return new HandBuilder(matchBuilder, playerName);
    }

    private final MatchBuilder matchBuilder;

    private final String playerName;
    private final List<CardValue> cards = new ArrayList<>();
    private Integer announcedScore;

    public HandBuilder(MatchBuilder matchBuilder, String playerName) {
      this.matchBuilder = matchBuilder;
      this.playerName = playerName;
    }

    public HandBuilder withCards(CardValue... cards) {
      this.cards.addAll(Arrays.stream(cards).collect(Collectors.toList()));
      return this;
    }

    public HandBuilder withAnnouncedScore(int announcedScore) {
      this.announcedScore = announcedScore;
      return this;
    }

    public MatchBuilder finishHand() {
      return matchBuilder.addHand(this);
    }
  }
}
