-- Development/demo seed for Santa Marta, Colombia.
-- All four accounts use password: Admin123!

INSERT INTO users (email, password_hash, full_name, active_mode, status, is_verified, created_at, updated_at)
VALUES
    ('turista@taminaka.com', '$2a$10$/0x0VlNC4HItxEaXjonNHuXE3no0ecdeZhriaWeNCOZRlPZ5l1ksi', 'Turista Tamaka', 'TURISTA', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('gestor@taminaka.com', '$2a$10$/0x0VlNC4HItxEaXjonNHuXE3no0ecdeZhriaWeNCOZRlPZ5l1ksi', 'Gestor Santa Marta', 'TURISTA', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('op@taminaka.com', '$2a$10$/0x0VlNC4HItxEaXjonNHuXE3no0ecdeZhriaWeNCOZRlPZ5l1ksi', 'Operador Tayrona', 'OPERADOR', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    is_verified = true,
    status = 'ACTIVE';

INSERT INTO user_roles (user_id, role)
SELECT id, 'TURISTA' FROM users WHERE email IN ('turista@taminaka.com', 'gestor@taminaka.com', 'op@taminaka.com')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'GESTOR' FROM users WHERE email = 'gestor@taminaka.com'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'OPERADOR' FROM users WHERE email = 'op@taminaka.com'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'admin@tamaka.com'
ON CONFLICT DO NOTHING;

INSERT INTO categories (name, description, status)
VALUES
    ('Playas', 'Playas, bahías y zonas costeras de Santa Marta.', 'ACTIVE'),
    ('Naturaleza', 'Parques, senderos, montañas y reservas naturales.', 'ACTIVE'),
    ('Patrimonio', 'Centro histórico, museos, iglesias y patrimonio cultural.', 'ACTIVE'),
    ('Gastronomía', 'Restaurantes, cafés y experiencias gastronómicas.', 'ACTIVE'),
    ('Aventura', 'Actividades de aventura, mar y montaña.', 'ACTIVE'),
    ('Bienestar', 'Experiencias de descanso, bienestar y relajación.', 'ACTIVE')
ON CONFLICT (name) DO NOTHING;

INSERT INTO operator_profiles (
    user_id, business_name, business_nit, business_address, business_phone,
    accreditation_text, accreditation_document_url, is_approved, approved_at
)
SELECT id, 'Tayrona Experience', '900123456-7', 'Carrera 2 # 18-45, Santa Marta',
       '+57 300 555 0188', 'Operador turístico de experiencias en la Sierra Nevada y el Caribe.',
       'https://example.com/acreditaciones/tayrona-experience.pdf', true, CURRENT_TIMESTAMP
FROM users
WHERE email = 'op@taminaka.com'
  AND NOT EXISTS (
      SELECT 1 FROM operator_profiles p
      JOIN users u ON u.id = p.user_id
      WHERE u.email = 'op@taminaka.com'
  );

INSERT INTO commerces (
    name, description, address, latitude, longitude, phone, email, website,
    images, schedules, status, owner_id, created_at, updated_at
)
SELECT 'Tayrona Experience',
       'Operador local de excursiones al Parque Tayrona, Ciudad Perdida y experiencias de naturaleza en Santa Marta.',
       'Carrera 2 # 18-45, Centro Histórico, Santa Marta',
       11.2417, -74.2053, '+57 300 555 0188', 'reservas@tayronaexperience.co',
       'https://tayronaexperience.co',
       'https://images.example.com/tayrona-experience.jpg',
       '[{"day":"LUNES-DOMINGO","open":"07:00","close":"19:00"}]',
       'ACTIVE', u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'op@taminaka.com'
  AND NOT EXISTS (SELECT 1 FROM commerces c WHERE c.name = 'Tayrona Experience');

INSERT INTO commerce_categories (commerce_id, category_id)
SELECT c.id, cat.id
FROM commerces c
JOIN categories cat ON cat.name IN ('Naturaleza', 'Aventura')
WHERE c.name = 'Tayrona Experience'
ON CONFLICT DO NOTHING;

INSERT INTO services (
    name, description, price, duration_minutes, images, status, is_visible,
    commerce_id, created_at, updated_at
)
SELECT 'Excursión Parque Tayrona - Cabo San Juan',
       'Transporte, acompañamiento y orientación para disfrutar Cabo San Juan y sus senderos.',
       180000, 600, 'https://images.example.com/cabo-san-juan.jpg',
       'ACTIVE', true, c.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM commerces c
WHERE c.name = 'Tayrona Experience'
  AND NOT EXISTS (
      SELECT 1 FROM services s
      WHERE s.name = 'Excursión Parque Tayrona - Cabo San Juan' AND s.commerce_id = c.id
  );

INSERT INTO services (
    name, description, price, duration_minutes, images, status, is_visible,
    commerce_id, created_at, updated_at
)
SELECT 'Caminata de un día a Minca',
       'Ruta de café, cascadas y miradores de la Sierra Nevada con guía local.',
       150000, 540, 'https://images.example.com/minca.jpg',
       'ACTIVE', true, c.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM commerces c
WHERE c.name = 'Tayrona Experience'
  AND NOT EXISTS (
      SELECT 1 FROM services s
      WHERE s.name = 'Caminata de un día a Minca' AND s.commerce_id = c.id
  );

INSERT INTO service_categories (service_id, category_id)
SELECT s.id, cat.id
FROM services s
JOIN categories cat ON cat.name IN ('Naturaleza', 'Aventura')
WHERE s.name IN ('Excursión Parque Tayrona - Cabo San Juan', 'Caminata de un día a Minca')
ON CONFLICT DO NOTHING;

INSERT INTO attractions (
    name, description, address, latitude, longitude, images, status,
    access_info, restrictions, capacity, current_occupancy, created_by, created_at, updated_at
)
SELECT 'Parque Nacional Natural Tayrona',
       'Uno de los destinos naturales más representativos del Caribe colombiano, con playas, bosque tropical y senderos.',
       'Entrada El Zaino, vía Santa Marta - Riohacha',
       11.2815, -73.9212, 'https://images.example.com/parque-tayrona.jpg',
       'ACTIVE', 'Ingreso por El Zaino o Calabazo; verificar horarios y condiciones del parque.',
       'Respetar las indicaciones ambientales; no extraer flora o fauna.',
       500, 180, u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'gestor@taminaka.com'
  AND NOT EXISTS (SELECT 1 FROM attractions WHERE name = 'Parque Nacional Natural Tayrona');

INSERT INTO attractions (
    name, description, address, latitude, longitude, images, status,
    access_info, restrictions, capacity, current_occupancy, created_by, created_at, updated_at
)
SELECT 'Playa El Rodadero',
       'Playa urbana de aguas tranquilas, actividades náuticas y una de las zonas turísticas más visitadas de Santa Marta.',
       'El Rodadero, Santa Marta',
       11.2047, -74.2294, 'https://images.example.com/el-rodadero.jpg',
       'ACTIVE', 'Acceso público por la carrera 1 y el malecón.',
       'Seguir las indicaciones de seguridad marítima.',
       800, 620, u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'gestor@taminaka.com'
  AND NOT EXISTS (SELECT 1 FROM attractions WHERE name = 'Playa El Rodadero');

INSERT INTO attractions (
    name, description, address, latitude, longitude, images, status,
    access_info, restrictions, capacity, current_occupancy, created_by, created_at, updated_at
)
SELECT 'Quinta de San Pedro Alejandrino',
       'Hacienda histórica y lugar donde murió Simón Bolívar, rodeada de jardines y espacios culturales.',
       'Avenida del Libertador, Santa Marta',
       11.2228, -74.1603, 'https://images.example.com/quinta-san-pedro.jpg',
       'ACTIVE', 'Acceso por la Avenida del Libertador; consultar horarios de visita.',
       'No ingresar alimentos a las salas; respetar las áreas patrimoniales.',
       300, 90, u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'gestor@taminaka.com'
  AND NOT EXISTS (SELECT 1 FROM attractions WHERE name = 'Quinta de San Pedro Alejandrino');

INSERT INTO attractions (
    name, description, address, latitude, longitude, images, status,
    access_info, restrictions, capacity, current_occupancy, created_by, created_at, updated_at
)
SELECT 'Ciudad Perdida',
       'Sitio arqueológico ancestral en la Sierra Nevada de Santa Marta, accesible mediante una travesía de varios días.',
       'Sierra Nevada de Santa Marta',
       11.0378, -73.9238, 'https://images.example.com/ciudad-perdida.jpg',
       'ACTIVE', 'Acceso únicamente con operador y guía autorizado.',
       'Requiere preparación física; respetar comunidades indígenas y normas ambientales.',
       120, 55, u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u
WHERE u.email = 'gestor@taminaka.com'
  AND NOT EXISTS (SELECT 1 FROM attractions WHERE name = 'Ciudad Perdida');

INSERT INTO attraction_categories (attraction_id, category_id)
SELECT a.id, cat.id
FROM attractions a
JOIN categories cat ON
    (a.name IN ('Parque Nacional Natural Tayrona', 'Ciudad Perdida') AND cat.name IN ('Naturaleza', 'Aventura'))
 OR (a.name = 'Playa El Rodadero' AND cat.name IN ('Playas', 'Aventura'))
 OR (a.name = 'Quinta de San Pedro Alejandrino' AND cat.name = 'Patrimonio')
ON CONFLICT DO NOTHING;

INSERT INTO attractions (
    name, description, address, latitude, longitude, images, status,
    access_info, restrictions, capacity, current_occupancy, service_id, created_by, created_at, updated_at
)
SELECT 'Experiencia Minca y cascadas',
       'Ruta de naturaleza y café conectada con el servicio de caminata de un día a Minca.',
       'Minca, Sierra Nevada de Santa Marta',
       11.1426, -74.1178, 'https://images.example.com/minca-cascadas.jpg',
       'ACTIVE', 'Coordinar transporte y guía con el operador.',
       'Usar calzado adecuado; respetar fuentes de agua.',
       100, 25, s.id, u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM services s
JOIN commerces c ON c.id = s.commerce_id
JOIN users u ON u.email = 'gestor@taminaka.com'
WHERE s.name = 'Caminata de un día a Minca'
  AND NOT EXISTS (SELECT 1 FROM attractions WHERE name = 'Experiencia Minca y cascadas');

INSERT INTO attraction_categories (attraction_id, category_id)
SELECT a.id, cat.id
FROM attractions a
JOIN categories cat ON cat.name IN ('Naturaleza', 'Aventura')
WHERE a.name = 'Experiencia Minca y cascadas'
ON CONFLICT DO NOTHING;

INSERT INTO events (
    name, description, event_type, start_date, end_date, location,
    latitude, longitude, images, status, attraction_id, created_by, created_at, updated_at
)
SELECT 'Atardecer cultural en la Quinta',
       'Recorrido cultural con música local y narración histórica en los jardines de la Quinta de San Pedro Alejandrino.',
       'DESTINO', CURRENT_TIMESTAMP + INTERVAL '10 days',
       CURRENT_TIMESTAMP + INTERVAL '10 days 3 hours',
       'Quinta de San Pedro Alejandrino', 11.2228, -74.1603,
       'https://images.example.com/atardecer-quinta.jpg', 'ACTIVE',
       a.id, u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM attractions a
JOIN users u ON u.email = 'gestor@taminaka.com'
WHERE a.name = 'Quinta de San Pedro Alejandrino'
  AND NOT EXISTS (SELECT 1 FROM events WHERE name = 'Atardecer cultural en la Quinta');

INSERT INTO events (
    name, description, event_type, start_date, end_date, location,
    latitude, longitude, images, status, commerce_id, created_by, created_at, updated_at
)
SELECT 'Salida grupal al Parque Tayrona',
       'Jornada guiada con cupos limitados para conocer los senderos y playas del Parque Tayrona.',
       'COMERCIO', CURRENT_TIMESTAMP + INTERVAL '15 days',
       CURRENT_TIMESTAMP + INTERVAL '15 days 10 hours',
       'Punto de encuentro: Centro Histórico de Santa Marta', 11.2417, -74.2053,
       'https://images.example.com/salida-tayrona.jpg', 'ACTIVE',
       c.id, u.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM commerces c
JOIN users u ON u.email = 'op@taminaka.com'
WHERE c.name = 'Tayrona Experience'
  AND NOT EXISTS (SELECT 1 FROM events WHERE name = 'Salida grupal al Parque Tayrona');
