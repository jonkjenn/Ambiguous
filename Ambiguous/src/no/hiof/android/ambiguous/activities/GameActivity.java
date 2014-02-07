package no.hiof.android.ambiguous.activities;

import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.DeckBuilder;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.RandomAmountGenerator;
import no.hiof.android.ambiguous.adapter.GameDeckAdapter;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Player;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.GridView;
import android.widget.TextView;

public class GameActivity extends Activity implements OnDragListener{
	private SQLiteDatabase db;
	private CardDataSource cs;
	private View layoutView;
	private GridView deckView;

	private Player player;
	private Player computer;
	
	private Random computerRandom;

	private enum states{PLAYER_TURN,PLAYER_DONE, COMPUTER_TURN};

	private states state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		layoutView = findViewById(R.id.game_layout);

        this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
        this.cs = new CardDataSource(db);

        List<Card> cards = cs.getCards();

        computer = new Player("Computer");
        computer.SetDeck(DeckBuilder.StandardDeck(cards));
		computerRandom = new Random();
        
        player = new Player("Jon");
        player.SetDeck(DeckBuilder.StandardDeck(cards));
        
        deckView = (GridView)findViewById(R.id.game_grid);
        GameDeckAdapter adapter = new GameDeckAdapter(player.GetCards());
        deckView.setAdapter(adapter);
        
        
        setupDragDrop(layoutView);

        state = (new Random().nextInt(1)==0?states.PLAYER_TURN:states.COMPUTER_TURN);

        changeState();
	}

	private void changeState()
	{
		switch(state)
		{
			case COMPUTER_TURN:
				computerTurn();
				break;
			case PLAYER_TURN:
				playerTurn();
				break;
			case PLAYER_DONE:
				playerDone();
				break;
			default:
				break;
		}
	}

	private void playerTurn()
	{
		this.setupDragDrop(this.layoutView);
		player.ModResource(2);
	}

	private void playerDone()
	{
		this.layoutView.setOnDragListener(null);
		updateStatsView();
        deckView.setAdapter(new GameDeckAdapter(player.GetCards()));
		state = states.COMPUTER_TURN;
		changeState();
	}
	
	private void updateStatsView()
	{
		TextView playerstats = (TextView)findViewById(R.id.stats_player);
		TextView computerstats = (TextView)findViewById(R.id.stats_computer);
		
		playerstats.setText(player.getStats());
		computerstats.setText(computer.getStats());
	}

	private void computerTurn()
	{
		computer.ModResource(2);
		int pos = computerRandom.nextInt(computer.GetCards().length-1);
		playCard(computer.GetCards()[pos],pos);
		state = states.PLAYER_TURN;		
		changeState();
	}
	
	private void playCard(int position)
	{
		if(state != states.PLAYER_TURN){return;}
		Card card = (Card)deckView.getItemAtPosition(position);
		playCard(card, position);
        changeState();
	}
	
	private void playCard(Card card,int position)
	{
		if(card == null){return;}
		switch(state)
		{
                case COMPUTER_TURN:
                		useCard(card,computer,player, position);
                        break;
                case PLAYER_TURN:
                		useCard(card,player,computer, position);
                        break;
                default:
                        break;
		}
	}
	
	private void useCard(Card card, Player caster, Player opponent, int position)
	{
		if(caster.UseResources(card.getCost()))
		{
                for(int i=0;i<card.getEffects().size();i++)
                {
                        Effect e = card.getEffects().get(i);
                        switch(e.getTarget())
                        {
						case OPPONENT:
							useEffect(e,opponent);
							break;
						case SELF:
							useEffect(e,caster);
							break;
						default:
							break;
                        }
                }
                caster.CardUsed(position);
                if(state == states.PLAYER_TURN)
                {
                        state = states.PLAYER_DONE;
                }
		}
	}
	
	private void useEffect(Effect e, Player target)
	{
		switch(e.getType())
		{
		case ARMOR:
			target.ModArmor(e.getMinValue());
			break;
		case DAMAGE:
			target.Damage(RandomAmountGenerator.GenerateAmount(e.getMinValue(), e.getMaxValue(), e.getCrit()));
			break;
		case HEALTH:
			target.Heal(RandomAmountGenerator.GenerateAmount(e.getMinValue(), e.getMaxValue(), e.getCrit()));
			break;
		case RESOURCE:
			target.ModResource(e.getMinValue());
			break;
		default:
			break;
		}

	}
	
	private void setupDragDrop(View view)
	{
		view.setOnDragListener(this);
	}

@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

        @Override
        public boolean onDrag(View v, DragEvent event) {
        if(state != states.PLAYER_TURN){return false;}

        switch(event.getAction())
        {
            case DragEvent.ACTION_DRAG_STARTED:
                            Log.d("test", "Drag started");
                        return true;

            case DragEvent.ACTION_DROP:
                        float y = event.getY();
                        if(event.getLocalState() != null)
                        {
                                int[] dragState = (int[])event.getLocalState();
                                float starty = (float)dragState[0];
                                Log.d("test",y + " " + starty);
                                if(y < starty-30){
                                	Log.d("test","Oppover");
                                	playCard(dragState[1]);
                                }
                                else if(y>starty+30){Log.d("test", "Nedover");}
                        }
                        return true;
        }
        return false;
        }

}