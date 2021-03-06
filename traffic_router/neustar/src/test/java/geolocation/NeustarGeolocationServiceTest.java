package geolocation;

import com.comcast.cdn.traffic_control.traffic_router.neustar.NeustarGeolocationService;
import com.comcast.cdn.traffic_control.traffic_router.neustar.data.NeustarDatabaseUpdater;
import com.comcast.cdn.traffic_control.traffic_router.geolocation.Geolocation;
import com.maxmind.db.Reader;
import com.quova.bff.reader.io.GPDatabaseReader;
import com.quova.bff.reader.model.GeoPointResponse;
import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.springframework.test.util.AssertionErrors.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NeustarGeolocationService.class, GPDatabaseReader.class, Reader.class})
public class NeustarGeolocationServiceTest {
	@Mock
	File neustarDatabaseDirectory;

	@InjectMocks
	NeustarGeolocationService service = new NeustarGeolocationService();

	@Before
	public void before() throws Exception {
		// This prevents extraneous output about 'WARN No appenders could be found....'
		LogManager.getRootLogger().addAppender(mock(Appender.class));

		initMocks(this);
		service.init();
	}

	@Test
	public void itNoLongerAllowsVerifyDatabase() throws Exception {
		try {
			service.verifyDatabase(neustarDatabaseDirectory);
			fail("Should have thrown RuntimeException when calling verifyDatabase");
		} catch (RuntimeException e) {
			assertThat(e.getMessage(), equalTo("verifyDatabase is no longer allowed, " + NeustarDatabaseUpdater.class.getSimpleName() + " is used for verification instead"));
		}
	}

	@Test
	public void itReturnsNullWhenDatabaseNotLoaded() throws Exception {
		assertThat(service.isInitialized(), equalTo(false));
		assertThat(service.location("192.168.99.100"), nullValue());
	}

	@Test
	public void itReturnsNullWhenDatabaseDoesNotExist() throws Exception {
		when(neustarDatabaseDirectory.getAbsolutePath()).thenReturn("/path/to/file/");

		assertThat(service.isInitialized(), equalTo(false));
		assertThat(service.location("192.168.99.100"), nullValue());

		service.reloadDatabase();

		assertThat(service.isInitialized(), equalTo(false));
		assertThat(service.location("192.168.99.100"), nullValue());
	}

	@Test
	@PrepareForTest({GPDatabaseReader.Builder.class, NeustarGeolocationService.class})
	public void itReturnsALocationWhenTheDatabaseIsLoaded() throws Exception {
		when(neustarDatabaseDirectory.exists()).thenReturn(true);
		when(neustarDatabaseDirectory.list()).thenReturn(new String[] {"foo.gpdb"});

		GeoPointResponse geoPointResponse = mock(GeoPointResponse.class);
		when(geoPointResponse.getCity()).thenReturn("Springfield");
		when(geoPointResponse.getLatitude()).thenReturn(40.0);
		when(geoPointResponse.getLongitude()).thenReturn(-105.0);
		when(geoPointResponse.getCountry()).thenReturn("United States");
		when(geoPointResponse.getCountryCode()).thenReturn("100");

		GPDatabaseReader gpDatabaseReader = mock(GPDatabaseReader.class);
		when(gpDatabaseReader.ipInfo(InetAddress.getByName("192.168.99.100"))).thenReturn(geoPointResponse);

		GPDatabaseReader.Builder builder = mock(GPDatabaseReader.Builder.class);
		when(builder.build()).thenReturn(gpDatabaseReader);
		whenNew(GPDatabaseReader.Builder.class).withArguments(neustarDatabaseDirectory).thenReturn(builder);

		service.reloadDatabase();

		assertThat(service.isInitialized(), equalTo(true));

		Geolocation geolocation = service.location("192.168.99.100");
		assertThat(geolocation.getCity(), equalTo("Springfield"));
		assertThat(geolocation.getLatitude(), equalTo(40.0));
		assertThat(geolocation.getLongitude(), equalTo(-105.0));
		assertThat(geolocation.getCountryName(), equalTo("United States"));
		assertThat(geolocation.getCountryCode(), equalTo("100"));


		assertThat(service.location("192.168.99.100"), notNullValue());
	}

	@After
	public void after() {
		service.destroy();
	}
}
