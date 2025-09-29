const crypto = require("crypto");
const Koa = require("koa");
const Router = require("@koa/router");
const bodyParser = require("koa-bodyparser");
const jwt = require("jsonwebtoken");

// Clave secreta (en producción usa una más fuerte y en variables de entorno)
const SECRET_KEY = "mi_secreto_super_seguro";

const app = new Koa();
const router = new Router();

// Middleware para parsear JSON
app.use(bodyParser());

var flights = [];
var users = [];
var bookings = [];
router.delete("/cleanup", (ctx) => {
  flights = [];
  users = [];
  bookings = [];
});

function insertFlight(ctx, input) {
  const countMissing = [
    "airlineName",
    "flightNumber",
    "estDepartureTime",
    "estArrivalTime",
    "availableSeats",
  ].filter((x) => input[x] == undefined || input[x] == null).length;
  if (countMissing) {
    ctx.throw(400);
  }

  if (!/^[A-Z]{2,3}[0-9]{3}$/.test(input.flightNumber)) {
    ctx.throw(400);
  }

  if (input.availableSeats === 0) {
    ctx.throw(400);
  }

  // unique
  if (flights.some((x) => x.flightNumber === input.flightNumber)) {
    ctx.throw(400);
  }

  // convert strings to dates
  input.estDepartureTime = new Date(input.estDepartureTime);
  input.estArrivalTime = new Date(input.estArrivalTime);

  const newID = crypto.randomUUID();
  flights.push({ ...input, id: newID });
  return newID;
}

// FLIGHTS
router.post("/flights/create", (ctx) => {
  const input = ctx.request.body;

  const newID = insertFlight(ctx, input);

  ctx.status = 201;
  ctx.body = { id: newID };
});

router.post("/flights/create-many", (ctx) => {
  const { inputs } = ctx.request.body;

  const newIDs = [];
  for (const input of inputs) {
    const newID = insertFlight(ctx, input);
    newIDs.push(newID);
  }

  ctx.status = 201;
  ctx.body = { ids: newIDs };
});

// FLIGHTS
router.post("/flights/create-many", (ctx) => {
  const input = ctx.request.body;

  const countMissing = [
    "airlineName",
    "flightNumber",
    "estDepartureTime",
    "estArrivalTime",
    "availableSeats",
  ].filter((x) => input[x] == undefined || input[x] == null).length;
  if (countMissing) {
    ctx.throw(400);
  }

  if (!/^[A-Z]{2,3}[0-9]{3}$/.test(input.flightNumber)) {
    ctx.throw(400);
  }

  if (input.availableSeats === 0) {
    ctx.throw(400);
  }

  // unique
  if (flights.some((x) => x.flightNumber === input.flightNumber)) {
    ctx.throw(400);
  }

  // convert strings to dates
  input.estDepartureTime = new Date(input.estDepartureTime);
  input.estArrivalTime = new Date(input.estArrivalTime);

  const newID = crypto.randomUUID();
  flights.push({ ...input, id: newID });

  ctx.status = 201;
  ctx.body = { id: newID };
});

// USERS
router.post("/users/register", (ctx) => {
  const input = ctx.request.body;

  const countMissing = ["firstName", "lastName", "email", "password"].filter(
    (x) => input[x] == undefined || input[x] == null
  ).length;
  if (countMissing) {
    ctx.throw(400);
  }

  if (
    !/^[a-z0-9_\.]+@[a-z0-9_\.]+\.[a-z]{2,3}(\.[a-z]{2})?$/.test(input.email) ||
    !/^[A-z]/.test(input.firstName) ||
    !/^[A-z]/.test(input.lastName) ||
    !(
      input.password.length >= 8 &&
      /[A-Z]/.test(input.password) &&
      /[0-9]/.test(input.password)
    )
  ) {
    ctx.throw(400);
  }

  // unique
  if (users.some((x) => x.email === input.email)) {
    ctx.throw(400);
  }

  const newID = crypto.randomUUID();
  users.push({ ...input, id: newID });

  ctx.status = 201;
  ctx.body = { id: newID };
});

// LOGIN
router.post("/auth/login", (ctx) => {
  const input = ctx.request.body;

  if (!input.password || !input.email) {
    ctx.throw(400);
  }

  const found = users.find(
    (x) => x.email === input.email && x.password === input.password
  );

  if (!found) {
    ctx.throw(400);
  }

  // Ejemplo de uso
  const token = jwt.sign({ sub: found.id, email: found.email }, SECRET_KEY, {
    expiresIn: "1h",
  });

  ctx.body = { token };
});

const authorize = (ctx, next) => {
  const token = ctx.headers.authorization?.startsWith("Bearer ")
    ? ctx.headers.authorization.slice("Bearer ".length)
    : null;

  if (!token) {
    ctx.throw(401);
  }

  try {
    ctx.session = jwt.verify(token, SECRET_KEY);
  } catch (err) {
    ctx.throw(401);
  }

  return next();
};

// SEARCH FLIGHT
router.get("/flights/search", authorize, (ctx) => {
  const input = ctx.query;
  ctx.body = {
    items: flights.filter((x) => {
      let result = true;

      if (input.flightNumber) {
        result &= x.flightNumber.includes(input.flightNumber);
      }

      if (input.airlineName) {
        result &= x.airlineName.includes(input.airlineName);
      }

      if (input.estDepartureTimeFrom) {
        result &=
          x.estDepartureTime.getTime() >=
          new Date(input.estDepartureTimeFrom).getTime();
      }

      if (input.estDepartureTimeTo) {
        result &=
          x.estDepartureTime.getTime() <=
          new Date(input.estDepartureTimeTo).getTime();
      }

      return result;
    }),
  };
});

// BOOK FLIGHT
router.post("/flights/book", authorize, (ctx) => {
  const user = users.find((x) => x.id === ctx.session.sub);

  if (!ctx.request.body.flightId) {
    ctx.throw(400, `flightId is required`);
  }

  const newFlight = flights.find((x) => x.id === ctx.request.body.flightId);
  if (!newFlight) {
    ctx.throw(404, `flight ${ctx.request.body.flightId} not found`);
  }

  if (newFlight.availableSeats === 0) {
    ctx.throw(400, `flight ${ctx.request.body.flightId} cannot be oversold`);
  }

  const now = new Date();
  if (
    newFlight.estDepartureTime.getTime() < now.getTime() ||
    newFlight.estArrivalTime.getTime() < now.getTime()
  ) {
    ctx.throw(400, `flight ${ctx.request.body.flightId} is in the past`);
  }

  // get other flights the user has booked (and it is not the same one)
  const sameUserFlightIds = bookings
    .filter((b) => b.customerId === user.id && b.flightId !== newFlight.id)
    .map((b) => b.flightId);

  const sameUserFlights = flights.filter((f) =>
    sameUserFlightIds.includes(f.id)
  );

  const overlappingFlights = sameUserFlights.filter(
    (x) =>
      newFlight.estDepartureTime < x.estArrivalTime &&
      newFlight.estArrivalTime > x.estDepartureTime
  );

  if (overlappingFlights.length > 0) {
    ctx.throw(400, "overlapping flight");
  }

  const newBooking = {
    id: crypto.randomUUID(),
    customerId: user.id,
    customerFirstName: user.firstName,
    customerLastName: user.lastName,
    bookingDate: new Date(),
    flightId: newFlight.id,
  };
  bookings.push(newBooking);

  newFlight.availableSeats -= 1;

  ctx.body = {
    id: newBooking.id,
  };
});

router.get("/flights/book/:id", authorize, (ctx) => {
  const found = bookings.find((x) => x.id === ctx.params.id);
  if (!found) {
    ctx.throw(404);
  }

  const flight = flights.find((x) => x.id === found.flightId);

  ctx.body = {
    ...found,
    flightNumber: flight.flightNumber,
    estDepartureTime: flight.estDepartureTime,
    estArrivalTime: flight.estArrivalTime,
  };
});

// Registrar rutas
app.use(router.routes()).use(router.allowedMethods());

// Levantar server
const PORT = 8080;
app.listen(PORT, () => {
  console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
});
