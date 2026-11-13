
package br.com.nextagon;

import br.com.nextagon.dto.AuthResponseDto;
import br.com.nextagon.dto.RegisterDto;
import br.com.nextagon.model.Role;
import br.com.nextagon.model.User;
import br.com.nextagon.repository.UserRepository;
import br.com.nextagon.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import br.com.nextagon.dto.request.AthleteProfileRequestDto;
import br.com.nextagon.dto.request.ProfessionalProfileRequestDto;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BackendApplicationTests {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void professionalRegistrationCreatesMarketplaceProfile() throws Exception {

		RegisterDto dto = buildRegisterDto(Role.PROFESSIONAL);

		AuthResponseDto response = authService.register(dto);

		assertNotNull(response);
		assertNotNull(response.getUser());

		String userId = response.getUser().getId();

		userRepository.flush();

		User professional = userRepository.findById(userId)
				.orElseThrow();

		assertEquals(Role.PROFESSIONAL, professional.getRole());
		assertTrue(professional.isActive());

		assertNotNull(professional.getProfessionalProfile());

		var profile = professional.getProfessionalProfile();

		assertTrue(profile.isAvailable());
		assertEquals(0.0, profile.getAverageRating());
		assertEquals(0, profile.getTotalReviews());
		assertEquals(userId, profile.getUser().getId());

		boolean foundInRepository = userRepository
				.findProfessionalsForMarketplace(
						Role.PROFESSIONAL,
						0.0,
						PageRequest.of(0, 50)
				)
				.getContent()
				.stream()
				.anyMatch(user -> user.getId().equals(userId));

		assertTrue(
				foundInRepository,
				"O profissional deve aparecer na consulta do marketplace"
		);

		MockHttpServletResponse httpResponse = mockMvc.perform(
				get("/marketplace/professionals")
						.param("page", "0")
						.param("size", "50")
						.param("minRating", "0")
		).andReturn().getResponse();

		assertEquals(200, httpResponse.getStatus());

		JsonNode result = objectMapper.readTree(
				httpResponse.getContentAsString()
		);

		JsonNode content = result.path("content");

		assertTrue(content.isArray());

		boolean foundInApi = false;

		for (JsonNode item : content) {
			if (userId.equals(item.path("id").asText())) {

				foundInApi = true;
				assertFalse(
						item.has("email"),
						"O marketplace não deve expor o e-mail do profissional"
				);

				assertEquals(
						0.0,
						item.path("averageRating").asDouble()
				);

				assertTrue(
						item.path("available").asBoolean()
				);

				break;
			}
		}

		assertTrue(
				foundInApi,
				"O profissional deve aparecer na API pública"
		);
	}

	@Test
	void athleteRegistrationDoesNotCreateProfessionalProfile() {

		RegisterDto dto = buildRegisterDto(Role.ATHLETE);

		AuthResponseDto response = authService.register(dto);

		assertNotNull(response);

		String userId = response.getUser().getId();

		userRepository.flush();

		User athlete = userRepository.findById(userId)
				.orElseThrow();

		assertEquals(Role.ATHLETE, athlete.getRole());
		assertNull(athlete.getProfessionalProfile());
	}

	private RegisterDto buildRegisterDto(Role role) {

		RegisterDto dto = new RegisterDto();

		ReflectionTestUtils.setField(
				dto,
				"name",
				"Usuario de Teste"
		);

		ReflectionTestUtils.setField(
				dto,
				"email",
				"teste-" + UUID.randomUUID() + "@example.com"
		);

		ReflectionTestUtils.setField(
				dto,
				"password",
				"SenhaSegura@123"
		);

		ReflectionTestUtils.setField(
				dto,
				"role",
				role
		);

		return dto;
	}

	@Test
	void deveDiferenciarCamposAusentesENulosDoAtleta()
			throws Exception {

		String json = """
            {
                "displayName": "Atleta Teste",
                "healthNotes": null,
                "goals": []
            }
            """;

		AthleteProfileRequestDto dto = objectMapper.readValue(
				json,
				AthleteProfileRequestDto.class
		);

		assertTrue(dto.isHealthNotesProvided());
		assertNull(dto.getHealthNotes());

		assertTrue(dto.isGoalsProvided());
		assertTrue(dto.getGoals().isEmpty());

		assertFalse(dto.isHeightProvided());
		assertFalse(dto.isWeightProvided());
		assertFalse(dto.isFitnessLevelProvided());
	}

	@Test
	void deveDiferenciarCamposAusentesENulosDoProfissional()
			throws Exception {

		String json = """
            {
                "displayName": "Profissional Teste",
                "bio": null,
                "specialties": [],
                "hourlyRate": 150.0
            }
            """;

		ProfessionalProfileRequestDto dto = objectMapper.readValue(
				json,
				ProfessionalProfileRequestDto.class
		);

		assertTrue(dto.isBioProvided());
		assertNull(dto.getBio());

		assertTrue(dto.isSpecialtiesProvided());
		assertTrue(dto.getSpecialties().isEmpty());

		assertTrue(dto.isHourlyRateProvided());
		assertEquals(150.0, dto.getHourlyRate());

		assertFalse(dto.isCertificatesProvided());
		assertFalse(dto.isAvailableProvided());
	}

	@Test
	void deveAtualizarPerfilAtletaSemApagarDados()
			throws Exception {

		AuthResponseDto auth = authService.register(
				buildRegisterDto(Role.ATHLETE)
		);

		String token = auth.getAccessToken();

		String cadastro = """
            {
                "displayName": "Atleta Teste",
                "height": 1.80,
                "weight": 75.0,
                "healthNotes": "Sem restrições",
                "goals": ["Hipertrofia"]
            }
            """;

		var primeiraResposta = mockMvc.perform(
				put("/profile/athlete")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(cadastro)
		).andReturn().getResponse();

		assertEquals(200, primeiraResposta.getStatus());

		String atualizacao = """
            {
                "displayName": "Atleta Teste",
                "weight": 80.0,
                "healthNotes": null,
                "goals": []
            }
            """;

		var resposta = mockMvc.perform(
				put("/profile/athlete")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(atualizacao)
		).andReturn().getResponse();

		assertEquals(200, resposta.getStatus());

		JsonNode json = objectMapper.readTree(
				resposta.getContentAsString()
		);

		assertEquals(1.80, json.path("height").asDouble());
		assertEquals(80.0, json.path("weight").asDouble());

		assertTrue(json.path("healthNotes").isNull());
		assertTrue(json.path("goals").isArray());
		assertEquals(0, json.path("goals").size());
	}

	@Test
	void deveAtualizarPerfilProfissionalSemApagarDados()
			throws Exception {

		AuthResponseDto auth = authService.register(
				buildRegisterDto(Role.PROFESSIONAL)
		);

		String token = auth.getAccessToken();

		String cadastro = """
            {
                "displayName": "Personal Trainer",
                "bio": "Profissional experiente",
                "hourlyRate": 100.0,
                "specialties": ["Musculação"],
                "certificates": ["CREF 12345"]
            }
            """;

		var primeiraResposta = mockMvc.perform(
				put("/profile/professional")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(cadastro)
		).andReturn().getResponse();

		assertEquals(200, primeiraResposta.getStatus());

		String atualizacao = """
            {
                "displayName": "Personal Trainer",
                "bio": null,
                "hourlyRate": 150.0,
                "specialties": []
            }
            """;

		var resposta = mockMvc.perform(
				put("/profile/professional")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(atualizacao)
		).andReturn().getResponse();

		assertEquals(200, resposta.getStatus());

		JsonNode json = objectMapper.readTree(
				resposta.getContentAsString()
		);

		assertEquals(150.0, json.path("hourlyRate").asDouble());
		assertTrue(json.path("bio").isNull());
		assertEquals(0, json.path("specialties").size());

		String userId = auth.getUser().getId();

		var profile = userRepository.findById(userId)
				.orElseThrow()
				.getProfessionalProfile();

		assertEquals(
				"CREF 12345",
				profile.getCertificates().get(0)
		);

		assertTrue(profile.isAvailable());
	}

	@Test
	void naoDevePermitirAtletaEditarPerfilProfissional()
			throws Exception {

		AuthResponseDto auth = authService.register(
				buildRegisterDto(Role.ATHLETE)
		);

		String json = """
            {
                "displayName": "Tentativa Indevida",
                "bio": "Teste"
            }
            """;

		var resposta = mockMvc.perform(
				put("/profile/professional")
						.header(
								"Authorization",
								"Bearer " + auth.getAccessToken()
						)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
		).andReturn().getResponse();

		assertEquals(403, resposta.getStatus());
	}

	@Test
	void naoDevePermitirEditarPerfilSemAutenticacao()
			throws Exception {

		String json = """
            {
                "displayName": "Usuario Sem Login"
            }
            """;

		var respostaAtleta = mockMvc.perform(
				put("/profile/athlete")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
		).andReturn().getResponse();

		var respostaProfissional = mockMvc.perform(
				put("/profile/professional")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
		).andReturn().getResponse();

		assertEquals(401, respostaAtleta.getStatus());
		assertEquals(401, respostaProfissional.getStatus());
	}

}
