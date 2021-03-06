package io.github.orionhealth.xbdd.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import io.github.orionhealth.xbdd.mappers.CoordinatesMapper;
import io.github.orionhealth.xbdd.model.common.CoordinatesDto;
import io.github.orionhealth.xbdd.model.common.Summary;
import io.github.orionhealth.xbdd.util.Coordinates;

@Repository
public class SummaryDao {

	@Autowired
	private MongoDatabase mongoBddDatabase;

	public List<Summary> getSummaries() {
		final MongoCollection<Summary> summary = getSummaryCollection();
		final List<Summary> rtn = new ArrayList<>();

		final Consumer<Summary> addToRtn = rtn::add;
		summary.find(Summary.class).forEach(addToRtn);

		return rtn;
	}

	public void updateSummary(final Coordinates coordinates) {
		final MongoCollection<Summary> summary = getSummaryCollection();

		final Bson query = Filters.eq(coordinates.getProduct() + "/" + coordinates.getVersionString());
		final Summary summaryObject = summary.find(query, Summary.class).first();

		if (summaryObject != null) { // lookup the summary document
			if (!summaryObject.getBuilds()
					.contains(coordinates.getBuild())) { // only update it if this build hasn't been added to it before.
				summaryObject.getBuilds().add(coordinates.getBuild());
				summary.replaceOne(query, summaryObject);
			}
		} else {
			final Summary newSummary = new Summary();
			newSummary.setId(coordinates.getProduct() + "/" + coordinates.getVersionString());
			newSummary.setBuilds(new ArrayList<>());
			newSummary.getBuilds().add(coordinates.getBuild());

			// Summary's don't care about the build as they have a list of them.
			final CoordinatesDto coordDto = CoordinatesMapper.mapCoordinates(coordinates);
			coordDto.setBuild(null);
			newSummary.setCoordinates(coordDto);

			summary.insertOne(newSummary);
		}
	}

	private MongoCollection<Summary> getSummaryCollection() {
		return this.mongoBddDatabase.getCollection("summary", Summary.class);
	}
}
