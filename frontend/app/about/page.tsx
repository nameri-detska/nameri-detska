import type { Metadata } from "next";
import { Baby, Database, FileText, Image } from "lucide-react";

export const metadata: Metadata = {
  title: "За проекта — Намери Детска",
  description:
    "Научи повече за проекта Намери Детска — защо съществува, как работи и какви публични източници на данни използва.",
};

export default function AboutPage() {
  return (
    <main className="bg-[var(--bg)]">
      <div className="mx-auto max-w-3xl px-4 sm:px-6 lg:px-8 py-12 md:py-16">
        {/* Header */}
        <div className="mb-12 text-center">
          <h1 className="text-3xl font-bold text-[var(--text)] md:text-4xl">
            За проекта
          </h1>
        </div>

        {/* Why this project exists */}
        <section
          className="rounded-2xl border border-[var(--border)] bg-[var(--surface)] shadow-[var(--shadow)] p-6 md:p-8 mb-6">
          <div className="flex items-center gap-3 mb-4">
            <div
              className="flex items-center justify-center w-10 h-10 rounded-xl bg-[var(--accent-soft)] text-[var(--accent)]">
              <Baby className="size-5"/>
            </div>
            <h2 className="text-xl font-bold text-[var(--text)]">
              Защо съществува този проект
            </h2>
          </div>
          <p className="text-[var(--text-secondary)] leading-relaxed">
            Като родител получих имейл, че детето ми не е прието на първо класиране в СДЯ. Започнах да търся частна
            ясла, одобрена от СРЗИ, която носи точка при последващо кандидатстване. Отне ми доста време да намеря
            най-актуалната версия на този списък, скрит дълбоко в сайта на ИСОДЗ в секция &quot;Ръководство на
            потребителя&quot;... От там прегледах яслите за нашия район и се оказаха на 30 минути пеша от нас. Дадох
            списъка на Chat GPT, за да ми намери по-близка, и в интерес на истината се справи доста добре и откри
            по-близки в други райони. Започнах да си задавам въпроси:
          </p>
          <ul className="mt-3 mb-6 space-y-2 text-[var(--text-secondary)] leading-relaxed list-disc list-inside">
            <li>Защо трябва да влагам толкова усилие заради проблем, породен от Столична община?</li>
            <li>Защо частните ясли, регистрирани от СРЗИ, не са нанесени на картата в ИСОДЗ?</li>
            <li>Защо няма функционалност да сортирам яслите по близост до мен?</li>
          </ul>

          <p className="text-[var(--text-secondary)] leading-relaxed">
            В крайна сметка извадихме късмет и ни приеха на второ класиране, но все пак реших да vibe code-на набързо
            едно демонстративно решение на проблемите, които аз срещнах. Ще предложа подобренията на ИСОДЗ и се надявам
            този сайт да може да бъде загасен завинаги догодина.
          </p>
        </section>

        {/* Demo screenshot */}
        <section
          className="rounded-2xl border border-[var(--border)] bg-[var(--surface)] shadow-[var(--shadow)] p-6 md:p-8 mb-6">
          <div className="flex items-center gap-3 mb-4">
            <div
              className="flex items-center justify-center w-10 h-10 rounded-xl bg-[var(--red-soft)] text-[var(--red)]">
              <Image className="size-5"/>
            </div>
            <h2 className="text-xl font-bold text-[var(--text)]">
              Резултатът
            </h2>
          </div>
          <p className="text-[var(--text-secondary)] leading-relaxed mb-4">
            Лесно мога да видя всичко около мен:
          </p>
          <div className="rounded-xl border border-[var(--border)] overflow-hidden mb-6">
            <img
              src="/demo-image.png"
              alt="Частни СРЗИ ясли около мен, сортирани по разстояние"
              className="w-full h-auto"
            />
          </div>
          <ul className="space-y-2 text-[var(--text-secondary)] leading-relaxed list-disc list-inside">
            <li>Автоматизирано прочитане на данните от публичното REST API на ИСОДЗ за общинските ясли и градини, от
              публичното REST API на МОН за частните градини и от СРЗИ PDF файла чрез LLM за частните ясли.
            </li>
            <li>Геокодиране на адресите от прочетените данни</li>
            <li>Визуализиране на една карта на всички</li>
            <li>Поддържане на отправен адрес и сортиране на яслите и градините по близост</li>
          </ul>
        </section>

        {/* Public resources */}
        <section
          className="rounded-2xl border border-[var(--border)] bg-[var(--surface)] shadow-[var(--shadow)] p-6 md:p-8 mb-6">
          <div className="flex items-center gap-3 mb-4">
            <div
              className="flex items-center justify-center w-10 h-10 rounded-xl bg-[var(--green-soft)] text-[var(--green)]">
              <Database className="size-5"/>
            </div>
            <h2 className="text-xl font-bold text-[var(--text)]">
              Използвани ресурси
            </h2>
          </div>

          <div>
            <div className="rounded-xl border border-[var(--border-light)] bg-[var(--bg-alt)] p-4">
              <div className="flex items-center gap-2 mb-2">
                <FileText className="size-4 text-[var(--accent)] shrink-0"/>
                <h3 className="text-sm font-semibold text-[var(--text)]">
                  Общински детски градини и ясли
                </h3>
              </div>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed mb-2">
                Публично REST API на ИСОДЗ, чрез което могат да се достъпят данните за всички общински детски градини,
                ясли (СДЯ) и градини с яслени групи.
              </p>
              <a
                href="https://kg.sofia.bg/api/public/kg/type/kinderGarden/all?filterType=by_region&kgType=0&regionId=0"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-block text-xs text-[var(--accent)] hover:underline break-all"
              >
                kg.sofia.bg/api/public/kg/type/kinderGarden/all?filterType=by_region&kgType=0&regionId=0
              </a>
            </div>

            <div className="rounded-xl border border-[var(--border-light)] bg-[var(--bg-alt)] p-4">
              <div className="flex items-center gap-2 mb-2">
                <FileText className="size-4 text-[var(--accent)] shrink-0"/>
                <h3 className="text-sm font-semibold text-[var(--text)]">
                  Частни детски градини, регистрирани от МОН
                </h3>
              </div>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed mb-2">
                Публично REST API на МОН, чрез което могат да се достъпят данните за всички частни детски градини
              </p>
              <a
                href="https://ri-api.mon.bg/data/get/public-register"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-block text-xs text-[var(--accent)] hover:underline break-all"
              >
                https://ri-api.mon.bg/data/get/public-register
              </a>
              <p className="text-xs text-[var(--text-secondary)] mt-3 mb-1">
                POST body (<code className="text-[11px] bg-[var(--border-light)] px-1 py-0.5 rounded">Content-Type:
                application/json</code>):
              </p>
              <pre
                className="rounded-lg border border-[var(--border-light)] bg-[var(--bg)] p-3 text-xs text-[var(--text)] overflow-x-auto leading-relaxed">
{`{
    "region": [22, 23],
    "instType": [2],
    "financialSchoolType": [3],
    "isRIActive": 1
}`}
              </pre>
              <a
                href="https://ri-api.mon.bg/data/get/institution"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-block text-xs text-[var(--accent)] hover:underline break-all"
              >
                https://ri-api.mon.bg/data/get/institution
              </a>
              <p className="text-xs text-[var(--text-secondary)] mt-3 mb-1">
                POST body (<code className="text-[11px] bg-[var(--border-light)] px-1 py-0.5 rounded">Content-Type:
                application/json</code>):
              </p>
              <pre
                className="rounded-lg border border-[var(--border-light)] bg-[var(--bg)] p-3 text-xs text-[var(--text)] overflow-x-auto leading-relaxed">
{`{
    "instid": "2200016",
    "procID": "9643"
}`}
              </pre>
              <p className="text-xs text-[var(--text-secondary)] mt-1.5 leading-relaxed">
                <code className="text-[11px] bg-[var(--border-light)] px-1 py-0.5 rounded">instid</code> и <code
                className="text-[11px] bg-[var(--border-light)] px-1 py-0.5 rounded">procID</code> се вземат от отговора
                на заявката към <code
                className="text-[11px] bg-[var(--border-light)] px-1 py-0.5 rounded">/public-register (за всеки един
                запис)</code>.
              </p>

            </div>

            <div className="rounded-xl border border-[var(--border-light)] bg-[var(--bg-alt)] p-4">
              <div className="flex items-center gap-2 mb-2">
                <FileText className="size-4 text-[var(--accent)] shrink-0"/>
                <h3 className="text-sm font-semibold text-[var(--text)]">
                  Частни детски ясли, регистрирани от СРЗИ
                </h3>
              </div>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed mb-2">
                Списък на частните детски ясли, регистрирани от СРЗИ (към 08.06.2026 г.). Публикува се в ИСОДЗ в секция
                &quot;Ръководство на потребителя&quot;.
              </p>
              <div className="space-y-1">
                <a
                  href="https://kg.sofia.bg/api/public/file/91f643b4bd6a4b179aaec3de09d028af"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-block text-xs text-[var(--accent)] hover:underline break-all"
                >
                  PDF файл
                </a>
                <span className="text-xs text-[var(--text-muted)]"> &middot; </span>
                <a
                  href="https://kg.sofia.bg/#/manual"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-block text-xs text-[var(--accent)] hover:underline"
                >
                  Източник: kg.sofia.bg/#/manual
                </a>
              </div>
            </div>

            <div className="rounded-xl border border-[var(--border-light)] bg-[var(--bg-alt)] p-4">
              <div className="flex items-center gap-2 mb-2">
                <FileText className="size-4 text-[var(--accent)] shrink-0"/>
                <h3 className="text-sm font-semibold text-[var(--text)]">
                  Геокодиране на адреси
                </h3>
              </div>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed mb-2">
                Въведените от потребителя адреси се геокодират чрез{" "}
                <a
                  href="https://nominatim.org/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--accent)] hover:underline"
                >
                  Nominatim (OpenStreetMap)
                </a>
                , безплатна услуга, която преобразува адреси в координати.
              </p>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
                Адресите на самите заведения се геокодират предварително чрез{" "}
                <a
                  href="https://developers.google.com/maps/documentation/geocoding/overview"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--accent)] hover:underline"
                >
                  Google Maps Geocoding API
                </a>
                . Забелязах, че има грешно геокодирани адреси, поради различни причини вкл. и невъзможност на Google
                Maps да намери адреса. Коригирах всички, които видях, вкл. намерих трудни адреси чрез Waze / Moovit -
                около 10% от общия брой заведения. Възможно е да има още грешни - ако видите, можете да ми пишете. Иначе
                това е област, в която системата би се радвала на подобрение, ако някой успее да постигне по-точен
                резултат.
              </p>
            </div>

            <div className="rounded-xl border border-[var(--border-light)] bg-[var(--bg-alt)] p-4">
              <div className="flex items-center gap-2 mb-2">
                <FileText className="size-4 text-[var(--accent)] shrink-0"/>
                <h3 className="text-sm font-semibold text-[var(--text)]">
                  Изчисляване на разстояния
                </h3>
              </div>
              <p className="text-sm text-[var(--text-secondary)] leading-relaxed mb-2">
                Разстоянията между две точки се изчисляват чрез{" "}
                <a
                  href="https://en.wikipedia.org/wiki/Haversine_formula"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--accent)] hover:underline"
                >
                  Haversine формула
                </a>
                {" "}по въздушна линия.
              </p>
            </div>
          </div>
        </section>
      </div>
    </main>
  );
}
