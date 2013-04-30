package net.frcdb.stats.chart;

import com.fasterxml.jackson.core.JsonGenerator;
import com.googlecode.objectify.annotation.EntitySubclass;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.frcdb.api.event.Event;
import net.frcdb.api.game.event.Game;
import net.frcdb.api.game.team.TeamEntry;
import net.frcdb.api.team.Team;
import net.frcdb.db.Database;
import net.frcdb.stats.chart.api.Chart;
import net.frcdb.stats.chart.api.ColumnDefinition;
import net.frcdb.stats.chart.api.Row;
import net.frcdb.stats.chart.api.YearChart;

/**
 * A chart to display the most common destinations for teams traveling out of
 * state. This is pretty damn expensive and will probably take ~3000 queries.
 * @author tim
 */
@EntitySubclass
public class TravelChart extends YearChart {

	public TravelChart() {
	}

	public TravelChart(int year) {
		super(year);
	}
	
	@Override
	public String getName() {
		return "team-travel-chart";
	}

	@Override
	public String getDisplayName() {
		return "Team Travel Destinations";
	}

	@Override
	public String getChartType() {
		return Chart.TYPE_GEO;
	}

	@Override
	public ColumnDefinition[] getColumns() {
		return new ColumnDefinition[] {
			column().label("Latitude").type("number").get(),
			column().label("Longitude").type("number").get(),
			column().label("Size").type("number").get(),
			column().label("Event").type("string").role("tooltip").get()
		};
	}

	@Override
	public List<Row> getRows() {
		Map<Event, Integer> counts = new HashMap<Event, Integer>();
		
		// iterate over all team entries
		for (Game g : getGames()) {
			Event e = g.getEvent();
			
			for (TeamEntry entry : g.getTeams()) {
				Team t = entry.getTeam();
				// count if the event is out of state
				if (!t.getState().equals(e.getState())) {
					if (counts.containsKey(e)) {
						counts.put(e, counts.get(e) + 1);
					} else {
						counts.put(e, 1);
					}
				}
			}
		}
		
		List<Row> ret = new ArrayList<Row>();
		
		for (Entry<Event, Integer> entry : counts.entrySet()) {
			Event e = entry.getKey();
			int c = entry.getValue();
			
			ret.add(row(
					e.getLatitude(),
					e.getLongitude(),
					c,
					e.getName() + ": " + c + " team" + (c != 1 ? "s" : "")));
		}
		
		return ret;
	}

	@Override
	public void writeOptions(JsonGenerator g) throws IOException {
		g.writeStringField("title", "Team Travel Destinations");
		g.writeStringField("displayMode", "markers");
		g.writeStringField("region", "US");
		
		g.writeObjectFieldStart("colorAxis");
		g.writeArrayFieldStart("colors");
		g.writeString("green");
		g.writeString("blue");
		g.writeEndArray();
		g.writeEndObject();
	}
	
}
