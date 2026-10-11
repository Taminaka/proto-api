-- Replace development placeholder image hosts with stable public Unsplash images.
UPDATE commerces
SET images = 'https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=1200&q=80'
WHERE images LIKE '%images.example.com%';

UPDATE services
SET images = 'https://images.unsplash.com/photo-1500534623283-312aade485b7?auto=format&fit=crop&w=1200&q=80'
WHERE images LIKE '%images.example.com%';

UPDATE attractions
SET images = CASE name
    WHEN 'Parque Nacional Natural Tayrona' THEN 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80'
    WHEN 'Playa El Rodadero' THEN 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80'
    WHEN 'Quinta de San Pedro Alejandrino' THEN 'https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=1200&q=80'
    WHEN 'Ciudad Perdida' THEN 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80'
    WHEN 'Experiencia Minca y cascadas' THEN 'https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=1200&q=80'
    ELSE images
END
WHERE images LIKE '%images.example.com%';

UPDATE events
SET images = CASE name
    WHEN 'Atardecer cultural en la Quinta' THEN 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1200&q=80'
    WHEN 'Salida grupal al Parque Tayrona' THEN 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=1200&q=80'
    ELSE images
END
WHERE images LIKE '%images.example.com%';
